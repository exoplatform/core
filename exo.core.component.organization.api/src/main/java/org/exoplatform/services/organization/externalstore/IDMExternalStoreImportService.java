package org.exoplatform.services.organization.externalstore;

import java.time.*;
import java.util.*;
import java.util.function.Consumer;

import org.apache.commons.lang.StringUtils;
import org.picocontainer.Startable;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.*;
import org.exoplatform.services.organization.externalstore.job.*;
import org.exoplatform.services.organization.externalstore.model.*;
import org.exoplatform.services.scheduler.CronJob;
import org.exoplatform.services.scheduler.JobSchedulerService;

/**
 * This class implements operations to import data from external IDM store to
 * internal store
 */
public class IDMExternalStoreImportService implements Startable {
  public static final Log         LOG                                       =
                                      ExoLogger.getLogger(IDMExternalStoreImportService.class);

  public static final String      EXTERNAL_STORE_IMPORT_CRON_EXPRESSION     = "exo.idm.externalStore.import.cronExpression";

  public static final String      EXTERNAL_STORE_DELETE_CRON_EXPRESSION     = "exo.idm.externalStore.delete.cronExpression";

  public static final String      IDM_QUEUE_PROCESSING_CRON_EXPRESSION      =
                                                                       "exo.idm.externalStore.queue.processing.cronExpression";

  private ExoContainer            container;

  private OrganizationService     organizationService;

  private ListenerService         listenerService;

  private IDMQueueService         idmQueueService;

  private IDMExternalStoreService externalStoreService;

  private JobSchedulerService     jobSchedulerService;

  private String                  scheduledDataImportJobCronExpression      = null;

  private String                  scheduledDataDeleteJobCronExpression      = null;

  private String                  scheduledQueueProcessingJobCronExpression = null;

  private boolean                 interrupted;

  public IDMExternalStoreImportService(ExoContainer container,
                                       OrganizationService organizationService,
                                       ListenerService listenerService,
                                       IDMExternalStoreService externalStoreService,
                                       JobSchedulerService jobSchedulerService,
                                       IDMQueueService idmQueueService,
                                       InitParams params) {
    this.container = container;
    this.organizationService = organizationService;
    this.listenerService = listenerService;
    this.idmQueueService = idmQueueService;
    this.externalStoreService = externalStoreService;
    this.jobSchedulerService = jobSchedulerService;
    if (params != null) {
      if (params.containsKey(EXTERNAL_STORE_IMPORT_CRON_EXPRESSION)) {
        scheduledDataImportJobCronExpression = params.getValueParam(EXTERNAL_STORE_IMPORT_CRON_EXPRESSION).getValue();
      }
      if (StringUtils.isBlank(scheduledDataImportJobCronExpression)) {
        LOG.warn("No scheduled job will be added to periodically import IDM data from external store");
      }
      if (params.containsKey(EXTERNAL_STORE_DELETE_CRON_EXPRESSION)) {
        scheduledDataDeleteJobCronExpression = params.getValueParam(EXTERNAL_STORE_DELETE_CRON_EXPRESSION).getValue();
      }
      if (StringUtils.isBlank(scheduledDataDeleteJobCronExpression)) {
        LOG.warn("No scheduled job will be added to periodically delete IDM data from internal store");
      }
      if (params.containsKey(IDM_QUEUE_PROCESSING_CRON_EXPRESSION)) {
        scheduledQueueProcessingJobCronExpression = params.getValueParam(IDM_QUEUE_PROCESSING_CRON_EXPRESSION).getValue();
      }
      if (StringUtils.isBlank(scheduledQueueProcessingJobCronExpression)) {
        LOG.warn("No scheduled job will be added to periodically process IDM Queue");
      }
    }
  }

  @Override
  public void start() {
    if (!externalStoreService.isEnabled()) {
      return;
    }
    RequestLifeCycle.begin(ExoContainerContext.getCurrentContainer());
    try {
      int queueEntriesInError = idmQueueService.count(idmQueueService.getMaxRetries());
      if (queueEntriesInError > 0) {
        LOG.warn("'{}' queue entries has exceeded max IDM Queue entry retry. Those queue items will remain on database. Pleaase check using JMX.",
                 queueEntriesInError);
      }

      initializeDataImportScheduledJob();
      initializeDataDeleteScheduledJob();
      initializeQueueProcessingScheduledJob();
    } catch (Exception e) {
      LOG.error("Error while configuring external store", e);
    } finally {
      forceCloseTransaction();
    }
  }

  @Override
  public void stop() {
  }

  /**
   * Check modifications made on external store and add corresponding entries to
   * queue to be treated by a queue processing Job. This operation is made
   * synchronized to avoid launch this function by migration, Job or manually in
   * parallel
   * 
   * @throws Exception
   */
  public synchronized void importAllModifiedEntitiesToQueue() throws Exception {
    checkModifiedEntitiesOfType(IDMEntityType.USER, getUserModificationConsumer(), true);
    checkModifiedEntitiesOfType(IDMEntityType.GROUP, getGroupModificationConsumer(), true);
    checkModifiedEntitiesOfType(IDMEntityType.ROLE, getRoleModificationConsumer(), true);
  }

  /**
   * Check modifications made on external store and add corresponding entries to
   * queue to be treated by a queue processing Job
   */
  public <T> void forceUpdateEntitiesOfType(IDMEntityType<T> entityType) {
    if (entityType == null) {
      throw new IllegalArgumentException("entityType is mandatory");
    }
    Set<IDMEntityType<?>> managedEntityTypes = externalStoreService.getManagedEntityTypes();
    if (!managedEntityTypes.contains(entityType)) {
      throw new IllegalArgumentException("entityType " + entityType.toString() + " is not managed by external store");
    }
    if (entityType.equals(IDMEntityType.USER)) {
      try {
        checkModifiedEntitiesOfType(IDMEntityType.USER, getUserModificationConsumer(), null);
      } catch (Exception e) {
        LOG.warn("An error occurred while importing users from external store", e);
      }
    } else if (entityType.equals(IDMEntityType.GROUP)) {
      try {
        checkModifiedEntitiesOfType(IDMEntityType.GROUP, getGroupModificationConsumer(), null);
      } catch (Exception e) {
        LOG.warn("An error occurred while importing groups from external store", e);
      }
    } else if (entityType.equals(IDMEntityType.ROLE)) {
      try {
        checkModifiedEntitiesOfType(IDMEntityType.ROLE, getRoleModificationConsumer(), null);
      } catch (Exception e) {
        LOG.warn("An error occurred while importing roles from external store", e);
      }
    }
  }

  /**
   * Check modifications made on external store and add corresponding entries to
   * queue to be treated by a queue processing Job
   * 
   * @param entityType
   * @throws Exception
   */
  public void importModifiedEntitiesOfTypeToQueue(IDMEntityType<?> entityType) throws Exception {
    if (entityType == null) {
      throw new IllegalArgumentException("entityType is mandatory");
    }
    if (entityType.equals(IDMEntityType.USER)) {
      checkModifiedEntitiesOfType(IDMEntityType.USER, getUserModificationConsumer(), true);
    } else if (entityType.equals(IDMEntityType.GROUP)) {
      checkModifiedEntitiesOfType(IDMEntityType.GROUP, getGroupModificationConsumer(), true);
    } else if (entityType.equals(IDMEntityType.ROLE)) {
      checkModifiedEntitiesOfType(IDMEntityType.ROLE, getRoleModificationConsumer(), true);
    }
  }

  /**
   * Check IDM entity information from external store. If modified, the consumer
   * parameter will be applied on entity. if updateLastCheckedTime is true, the
   * last check time will be stored.
   * 
   * @param entityType
   * @param modificationConsumer
   * @param updateLastCheckedTime
   * @throws Exception
   */
  public synchronized <T> void checkModifiedEntitiesOfType(IDMEntityType<T> entityType,
                                                           Consumer<String> modificationConsumer,
                                                           boolean updateLastCheckedTime) throws Exception {
    if (entityType == null) {
      throw new IllegalArgumentException("entityType is mandatory");
    }
    if (modificationConsumer == null) {
      throw new IllegalArgumentException("modificationConsumer is mandatory");
    }
    if (!externalStoreService.getManagedEntityTypes().contains(entityType)) {
      LOG.trace("Entity type " + entityType.getClassType().getName()
          + " is not managed by IDMExternalStoreService, check modified entities of this type will not proceed");
      return;
    }

    LocalDateTime localDateTime = getLocalDateTime();
    LocalDateTime lastSuccessExecutionTime = idmQueueService.getLastCheckedTime(entityType);
    if (lastSuccessExecutionTime != null) {
      // Remove one second to ensure that we have got
      // all modified entries, and no problem if we get a modified entry twice.
      // In fact, if the entry is modified/added at the exact same time (second)
      // we are checking the list of modified entries, we may miss some entries.
      lastSuccessExecutionTime = lastSuccessExecutionTime.minusSeconds(1);
    }

    checkModifiedEntitiesOfType(entityType, modificationConsumer, lastSuccessExecutionTime);
    if (updateLastCheckedTime) {
      idmQueueService.setLastCheckedTime(entityType, localDateTime);
    }
  }

  /**
   * Check IDM entity information from external store. If modified, the consumer
   * parameter will be applied on entity. if lastSuccessExecutionTime is null,
   * all entities will be retrieved
   * 
   * @param entityType
   * @param modificationConsumer
   * @param lastSuccessExecutionTime
   * @throws Exception
   */
  public synchronized <T> void checkModifiedEntitiesOfType(IDMEntityType<T> entityType,
                                                           Consumer<String> modificationConsumer,
                                                           LocalDateTime lastSuccessExecutionTime) throws Exception {
    if (entityType == null) {
      throw new IllegalArgumentException("entityType is mandatory");
    }
    if (modificationConsumer == null) {
      throw new IllegalArgumentException("modificationConsumer is mandatory");
    }
    if (!externalStoreService.getManagedEntityTypes().contains(entityType)) {
      LOG.trace("Entity type " + entityType.getClassType().getName()
          + " is not managed by IDMExternalStoreService, check modified entities of this type will not proceed");
      return;
    }

    if (IDMEntityType.USER.equals(entityType)) {
      loadModifiedUsers(entityType, modificationConsumer, lastSuccessExecutionTime);
    } else if (IDMEntityType.GROUP.equals(entityType)) {
      ListAccess<String> modifiedOrAddedGroups = externalStoreService.getAllOfType(IDMEntityType.GROUP, lastSuccessExecutionTime);
      if (modifiedOrAddedGroups == null) {
        return;
      }
      String[] groupIds = modifiedOrAddedGroups.load(0, Integer.MAX_VALUE);
      for (String groupId : groupIds) {
        if (interrupted) {
          throw new InterruptedException("Import of modified users check is interrupted, it will be retried next startup");
        }
        modificationConsumer.accept(groupId);
      }
    } else if (IDMEntityType.ROLE.equals(entityType)) {
      ListAccess<String> modifiedOrAddedRoles = externalStoreService.getAllOfType(IDMEntityType.ROLE, lastSuccessExecutionTime);
      String[] membershipTypes = modifiedOrAddedRoles.load(0, Integer.MAX_VALUE);
      for (String membershipType : membershipTypes) {
        if (interrupted) {
          throw new InterruptedException("Import of modified users check is interrupted, it will be retried next startup");
        }
        modificationConsumer.accept(membershipType);
      }
    }
  }

  /**
   * Apply changes added to queue to internal store. The change could be of
   * type, Add/Modify or Delete on Group/User or Role.
   */
  public synchronized void processQueueEntries() {
    try {
      for (int i = idmQueueService.getMaxRetries() - 1; i >= 0; i--) {
        int countAllEntitiesToImport = idmQueueService.count(i);
        int countEntitiesToImport = countAllEntitiesToImport;

        // Try to process new entries
        while (countEntitiesToImport > 0) {
          int numberOfTransactionLevels = forceCloseTransaction();
          try {
            RequestLifeCycle.begin(container);
            try {
              List<IDMQueueEntry> queueEntries = idmQueueService.pop(100, i, true);
              List<IDMQueueEntry> treatedQueueEntries = processQueueEntries(queueEntries);
              idmQueueService.storeAsProcessed(treatedQueueEntries);
            } finally {
              RequestLifeCycle.end();
            }
          } finally {
            openTransactionLevels(numberOfTransactionLevels);
          }
          if (interrupted) {
            throw new InterruptedException("Queue entries processing is interrupted, it will be retried next startup");
          }
          countEntitiesToImport = idmQueueService.count(i);
          LOG.info("Treated entities with number of retries = {} : {}/{}",
                   i,
                   (countAllEntitiesToImport - countEntitiesToImport),
                   countAllEntitiesToImport);
        }
      }
    } catch (Exception e) {
      LOG.error("An error occurred while processing queue", e);
    }
    try {
      idmQueueService.deleteProcessedEntries();
    } catch (Exception e) {
      LOG.error("An error occurred while deleting processed queue elements", e);
    }
  }

  /**
   * Imports entity information from external store if modified and trigger
   * event on listenerService. If updateModified is true, the entity will be
   * check if it was modified on external store and it will be updated on
   * internal consequently. If updateDeleted is true, the entity will be check
   * if it was deleted from external store and it will be deleted from internal
   * store consequently.
   * 
   * @param entityType
   * @param entityId
   * @param updateModified
   * @param updateDeleted
   * @throws Exception
   */
  public <T> T importEntityToInternalStore(IDMEntityType<T> entityType,
                                                        Object entityId,
                                                        boolean updateModified,
                                                        boolean updateDeleted) throws Exception {
    if (entityType == null) {
      throw new IllegalArgumentException("entityType is mandatory");
    }
    if (entityId == null || StringUtils.isBlank(entityId.toString())) {
      throw new IllegalArgumentException("entityId is mandatory");
    }

    boolean deleted = false;
    if (!externalStoreService.isEntityPresent(entityType, entityId)) {
      LOG.trace("Entity of type '{}' and id '{}' are not present in external store.", entityType.getName(), entityId);
      if (updateDeleted) {
        deleted = true;
      } else {
        return null;
      }
    }

    if (deleted) {
      LOG.info("Delete from internal store entity of type '{}' with id '{}'", entityType.getName(), entityId);
    } else {
      LOG.info("Import to internal store entity of type '{}' with id '{}'", entityType.getName(), entityId);
    }
    Object result = null;
    if (IDMEntityType.USER.equals(entityType)) {
      String username = entityId.toString();
      result = importUser(username, deleted, updateModified, updateDeleted);
    } else if (IDMEntityType.USER_PROFILE.equals(entityType)) {
      String username = entityId.toString();
      result = importUserProfile(username, deleted);
    } else if (IDMEntityType.GROUP.equals(entityType)) {
      String groupId = entityId.toString();
      result = importGroup(groupId, deleted, updateModified, updateDeleted);
    } else if (IDMEntityType.USER_MEMBERSHIPS.equals(entityType)) {
      String username = entityId.toString();
      importUserMemberships(username, updateModified, updateDeleted);
    } else if (IDMEntityType.GROUP_MEMBERSHIPS.equals(entityType)) {
      String groupId = entityId.toString();
      importGroupMemberships(groupId, updateModified, updateDeleted);
    } else if (IDMEntityType.ROLE.equals(entityType)) {
      String membershipTypeName = entityId.toString();
      result = importMembershipType(membershipTypeName, deleted);
    }
    LOG.info("Entity of type '{}' with id '{}' proceeded successfully", entityType.getName(), entityId);
    return result == null ? null : entityType.getClassType().cast(result);
  }

  /**
   * Check removal made on external store and add corresponding entries to queue
   * to be treated by a queue processing Job. This operation is made
   * synchronized to avoid launch this function by Job or manually in parallel
   * 
   * @throws Exception
   */
  public synchronized void checkAllEntitiesToDeleteIntoQueue() throws Exception {
    checkEntitiesToDeleteIntoQueue(IDMEntityType.USER);
    checkEntitiesToDeleteIntoQueue(IDMEntityType.GROUP);
  }

  /**
   * Check deletion made on external store and add corresponding entries to
   * queue to be treated by a queue processing Job
   * 
   * @param entityType
   * @throws Exception
   */
  public synchronized void checkEntitiesToDeleteIntoQueue(IDMEntityType<?> entityType) throws Exception {
    if (entityType == null) {
      throw new IllegalArgumentException("entityType is mandatory");
    }
    if (!externalStoreService.getManagedEntityTypes().contains(entityType)) {
      LOG.trace("Entity type " + entityType.getClassType().getName()
          + " is not managed by IDMExternalStoreService, check deleted entities of this type will not proceed");
      return;
    }

    if (entityType.equals(IDMEntityType.USER)) {
      LOG.info("Check deleted users from external store");
      checkDeletedEntitiesOfType(IDMEntityType.USER, getUserDeletionConsumer());
    } else if (entityType.equals(IDMEntityType.GROUP)) {
      LOG.info("Check deleted groups from external store");
      checkDeletedEntitiesOfType(IDMEntityType.GROUP, getGroupDeletionConsumer());
    }
  }

  /**
   * A generic method that will detects deletion of a USER or a GROUP from
   * external store. The consumer parameter will determine the behavior to do
   * with detected deleted entities
   * 
   * @param entityType
   * @param deletionConsumer
   * @throws Exception
   */
  public synchronized <T> void checkDeletedEntitiesOfType(IDMEntityType<T> entityType,
                                                          Consumer<String> deletionConsumer) throws Exception {
    if (entityType == null) {
      throw new IllegalArgumentException("entityType is mandatory");
    }
    if (deletionConsumer == null) {
      throw new IllegalArgumentException("modificationConsumer is mandatory");
    }
    if (!externalStoreService.getManagedEntityTypes().contains(entityType)) {
      LOG.trace("Entity type " + entityType.getClassType().getName()
          + " is not managed by IDMExternalStoreService, check deleted entities of this type will not proceed");
      return;
    }

    if (!externalStoreService.getManagedEntityTypes().contains(entityType)) {
      LOG.trace("Entity type " + entityType.getClassType().getName()
          + " is not managed by IDMExternalStoreService, check deleted entities of this type will not proceed");
      return;
    }

    if (IDMEntityType.USER.equals(entityType)) {
      ListAccess<User> allStoredUsersInInternalStore = externalStoreService.getAllInternalUsers();

      LOG.info("Retrieving all existing external users");
      List<String> externalUsernames = new ArrayList<>();
      loadModifiedUsers(entityType, (username) -> externalUsernames.add(username), null);
      LOG.info("{} users was retrieved from external store", externalUsernames.size());

      int totalUsers = allStoredUsersInInternalStore.getSize();
      int index = 0;
      int offset = 0;
      int limit = 100;
      while (index < totalUsers) {
        if (index + limit > totalUsers) {
          limit = totalUsers - index;
        }
        User[] users = allStoredUsersInInternalStore.load(offset, limit);
        if (users == null || users.length == 0) {
          break;
        }
        offset += limit;
        for (User user : users) {
          if (interrupted) {
            throw new InterruptedException("Deleted users check is interrupted, it will be retried next startup");
          }
          index++;
          String username = user.getUserName();
          if (!externalUsernames.contains(username)) {
            // since the user is retrieved from Hibernate with only the field
            // username, we should retrieve it again from internal store with
            // all attributes
            user = organizationService.getUserHandler().findUserByName(username);
            if (!user.isInternalStore()) {
              deletionConsumer.accept(username);
            }
          }
        }
        LOG.info("{}/{} was checked if deleted from external store", offset, totalUsers);
      }
    } else if (IDMEntityType.GROUP.equals(entityType)) {
      Collection<Group> allGroups = organizationService.getGroupHandler().getAllGroups();
      LOG.info("{} groups to check if deleted from external store", allGroups.size());
      for (Group group : allGroups) {
        if (interrupted) {
          throw new InterruptedException("Deleted users check is interrupted, it will be retried next startup");
        }
        if (!group.isInternalStore() && !externalStoreService.isEntityPresent(IDMEntityType.GROUP, group.getId())) {
          deletionConsumer.accept(group.getId());
        }
      }
      LOG.info("End groups deletion check from external store");
    }
  }

  public void interrupt() {
    interrupted = true;
  }

  private Consumer<String> getGroupDeletionConsumer() {
    Consumer<String> groupDeletionConsumer = (groupId) -> {
      LOG.info("Group '{}' is deleted from external store, a new IDM queue entry will be added", groupId);
      try {
        idmQueueService.push(new IDMQueueEntry(IDMEntityType.GROUP, groupId, IDMOperationType.DELETE));
      } catch (Exception e) {
        throw new RuntimeException("Unable to push Group '" + groupId + "' entry to delete on queue", e);
      }
    };
    return groupDeletionConsumer;
  }

  private Consumer<String> getUserDeletionConsumer() {
    Consumer<String> userDeletionConsumer = (username) -> {
      LOG.info("User '{}' is deleted from external store, a new IDM queue entry will be added", username);
      try {
        idmQueueService.push(new IDMQueueEntry(IDMEntityType.USER, username, IDMOperationType.DELETE));
      } catch (Exception e) {
        throw new RuntimeException("Unable to push user '" + username + "' entry to delete on queue", e);
      }
    };
    return userDeletionConsumer;
  }

  private Consumer<String> getRoleModificationConsumer() {
    Consumer<String> roleModificationConsumer = (membershipType) -> {
      try {
        if (organizationService.getMembershipTypeHandler().findMembershipType(membershipType) == null) {
          LOG.info("Role '{}' is added/updated from external store, a new IDM queue entry will be added", membershipType);
          idmQueueService.push(new IDMQueueEntry(IDMEntityType.ROLE, membershipType, IDMOperationType.ADD_OR_UPDATE));
        }
      } catch (Exception e) {
        throw new RuntimeException("Unable to push Role '" + membershipType + "' entry to add/modify on queue", e);
      }
    };
    return roleModificationConsumer;
  }

  private Consumer<String> getGroupModificationConsumer() {
    Consumer<String> groupModificationConsumer = (groupId) -> {
      LOG.info("Group '{}' is added/updated from external store, a new IDM queue entry will be added", groupId);
      try {
        idmQueueService.push(new IDMQueueEntry(IDMEntityType.GROUP, groupId, IDMOperationType.ADD_OR_UPDATE));
      } catch (Exception e) {
        throw new RuntimeException("Unable to push Group '" + groupId + "' entry to add/modify on queue", e);
      }
    };
    return groupModificationConsumer;
  }

  private Consumer<String> getUserModificationConsumer() {
    Consumer<String> userModificationConsumer = (username) -> {
      LOG.info("User '{}' is added/modified in external store, a new IDM queue entry will be added", username);
      try {
        idmQueueService.push(new IDMQueueEntry(IDMEntityType.USER, username, IDMOperationType.ADD_OR_UPDATE));
      } catch (Exception e) {
        throw new RuntimeException("Unable to push User '" + username + "' entry to add/modify on queue", e);
      }
    };
    return userModificationConsumer;
  }

  private MembershipType importMembershipType(String membershipTypeName, boolean deleted) throws Exception {
    MembershipType membershipType = organizationService.getMembershipTypeHandler().findMembershipType(membershipTypeName);
    if (deleted && membershipType != null) {
      LOG.trace("Remove from internal store deleted membershipType '{}' from external store", membershipTypeName);
      organizationService.getMembershipTypeHandler().removeMembershipType(membershipTypeName, true);

      return membershipType;
    }
    if (membershipType == null) {
      membershipType = externalStoreService.getEntity(IDMEntityType.ROLE, membershipTypeName);
      if (membershipType != null) {
        LOG.trace("Add in internal store new membershipType '{}' from external store", membershipTypeName);
        organizationService.getMembershipTypeHandler().createMembershipType(membershipType, true);
      }
    }
    return membershipType;
  }

  @SuppressWarnings({ "rawtypes", "deprecation" })
  private void importGroupMemberships(String groupId, boolean updateModified, boolean updateDeleted) throws Exception {
    Group group = organizationService.getGroupHandler().findGroupById(groupId);

    Collection internalMemberships = null;
    if (group == null) {
      internalMemberships = Collections.emptyList();
    } else {
      internalMemberships = organizationService.getMembershipHandler().findMembershipsByGroup(group);
    }
    Collection externalMemberships = externalStoreService.getEntity(IDMEntityType.GROUP_MEMBERSHIPS, groupId);
    importMemberships(IDMEntityType.GROUP_MEMBERSHIPS, externalMemberships, internalMemberships);
  }

  @SuppressWarnings({ "rawtypes" })
  private void importUserMemberships(String username, boolean updateModified, boolean updateDeleted) throws Exception {
    Collection internalMemberships = organizationService.getMembershipHandler().findMembershipsByUser(username);
    Collection externalMemberships = externalStoreService.getEntity(IDMEntityType.USER_MEMBERSHIPS, username);
    importMemberships(IDMEntityType.USER_MEMBERSHIPS, externalMemberships, internalMemberships);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private void importMemberships(IDMEntityType<?> entityType,
                                 Collection externalMemberships,
                                 Collection internalMemberships) throws Exception {
    if (externalMemberships != null && !externalMemberships.isEmpty()) {
      List membershipsToAdd = new ArrayList(externalMemberships);
      for (Object object : externalMemberships) {
        if (internalMemberships.contains(object)) {
          membershipsToAdd.remove(object);
        }
      }

      for (Object object : membershipsToAdd) {
        Membership membership = (Membership) object;
        importMembership(entityType, membership, false, false);
      }
    }

    // Check deleted user memberships on external store
    for (Object object : internalMemberships) {
      Membership membership = (Membership) object;
      Group group = organizationService.getGroupHandler().findGroupById(membership.getGroupId());
      User user = organizationService.getUserHandler().findUserByName(membership.getUserName(), UserStatus.ANY);

      if (group == null || user == null || (!group.isInternalStore() && !user.isInternalStore()
          && (externalMemberships == null || !externalMemberships.contains(membership)))) {
        LOG.trace("Remove deleted membership from external store '{}'", membership);
        organizationService.getMembershipHandler().removeMembership(membership.getId(), true);
      }
    }
  }

  private void importMembership(IDMEntityType<?> entityType,
                                Membership membership,
                                boolean updateModified,
                                boolean updateDeleted) throws Exception {
    String groupId = membership.getGroupId();
    Group group = organizationService.getGroupHandler().findGroupById(groupId);
    if (group == null) {
      // If user membership contains a not already imported group, skip it until
      // the group is imported with its memberships
      if (entityType.equals(IDMEntityType.USER_MEMBERSHIPS)) {
        return;
      }
      group = importEntityToInternalStore(IDMEntityType.GROUP, groupId, updateModified, updateDeleted);
      if (group == null) {
        LOG.warn("Can't add membership '{}'. The group '{}' is not found in internal store", membership.toString(), groupId);
        return;
      }
    }

    User user = organizationService.getUserHandler().findUserByName(membership.getUserName(), UserStatus.ANY);
    if (user == null) {
      user = importEntityToInternalStore(IDMEntityType.USER, membership.getUserName(), updateModified, updateDeleted);
      if (user == null) {
        // The subGroup of a Group can sometimes be retrieved as child user
        // member Which could lead to this use case. The following check is
        // added to be sure not displaying a log entry for a fake problem
        Group subGroup = organizationService.getGroupHandler().findGroupById(group.getId() + "/" + membership.getUserName());
        if (subGroup == null) {
          LOG.warn("Can't add membership '{}'. The user '{}' is not found in internal store",
                   membership.toString(),
                   membership.getUserName());
        } else {
          LOG.trace("Membership '{}' seems having retrieved a child group as a user '{}'",
                    membership.toString(),
                    membership.getUserName());
        }
        return;
      }
    }

    String membershipTypeName = membership.getMembershipType();
    MembershipType membershipType = organizationService.getMembershipTypeHandler().findMembershipType(membershipTypeName);
    if (membershipType == null) {
      membershipType = importEntityToInternalStore(IDMEntityType.ROLE, membershipTypeName, updateModified, updateDeleted);
      if (membershipType == null) {
        LOG.warn("Can't add membership '{}'. The membershipType '{}' is not found in internal store",
                 membership.toString(),
                 membershipTypeName);
        return;
      }
    }

    LOG.trace("Add new membership from external store '{}'", membership);
    organizationService.getMembershipHandler().linkMembership(user, group, membershipType, true);
  }

  private Group importGroup(String groupId, boolean deleted, boolean updateModified, boolean updateDeleted) throws Exception {
    Group group = organizationService.getGroupHandler().findGroupById(groupId);
    if (deleted && group != null) {
      LOG.trace("Remove from internal store deleted group '{}' from external store", groupId);
      organizationService.getGroupHandler().removeGroup(group, true);

      // Triggering event is optional, thus exceptions must be catched
      try {
        listenerService.broadcast(IDMExternalStoreService.GROUP_DELETED_FROM_EXTERNAL_STORE, this, group);
      } catch (Exception e) {
        LOG.warn("Error while triggering event on group '" + groupId + "' data import (delete) from external store", e);
      }
      return group;
    }

    boolean isNew = group == null;
    group = externalStoreService.getEntity(IDMEntityType.GROUP, groupId);
    if (group == null) {
      throw new IllegalStateException("Could not find group from external store with id " + groupId);
    }
    group.setOriginatingStore(OrganizationService.EXTERNAL_STORE);

    String parentId = group.getParentId();
    Group parentGroup = null;
    if (StringUtils.isNotBlank(parentId)) {
      parentGroup = organizationService.getGroupHandler().findGroupById(parentId);
      if (parentGroup == null) {
        parentGroup = importEntityToInternalStore(IDMEntityType.GROUP, parentId, updateModified, updateDeleted);
      }
      if (parentGroup == null) {
        throw new IllegalStateException("Can't find parent group with id " + parentId);
      }
    }
    if (isNew) {
      organizationService.getGroupHandler().addChild(parentGroup, group, true);
      // Triggering event is optional, thus exceptions must be catched
      try {
        listenerService.broadcast(IDMExternalStoreService.GROUP_ADDED_FROM_EXTERNAL_STORE, this, group);
      } catch (Exception e) {
        LOG.warn("Error while triggering event on group '" + groupId + "' data import (creation) from external store", e);
      }
    } else {
      organizationService.getGroupHandler().saveGroup(group, true);
      // Triggering event is optional, thus exceptions must be catched
      try {
        listenerService.broadcast(IDMExternalStoreService.GROUP_MODIFIED_FROM_EXTERNAL_STORE, this, group);
      } catch (Exception e) {
        LOG.warn("Error while triggering event on group '" + groupId + "' data import (modification) from external store", e);
      }
    }
    return group;
  }

  private UserProfile importUserProfile(String username, boolean deleted) throws Exception {
    UserProfile internalUserProfile = organizationService.getUserProfileHandler().findUserProfileByName(username);
    if (deleted && internalUserProfile != null) {
      LOG.trace("Remove from internal store deleted user profile '{}' from external store", username);
      organizationService.getUserProfileHandler().removeUserProfile(username, true);
    }

    UserProfile externalUserProfile = externalStoreService.getEntity(IDMEntityType.USER_PROFILE, username);

    if (externalUserProfile != null && externalUserProfile.getUserInfoMap() != null
        && !externalUserProfile.getUserInfoMap().isEmpty()) {
      Map<String, String> externalUserInfoMap = externalUserProfile.getUserInfoMap();
      Map<String, String> internalUserInfoMap = (internalUserProfile == null
          || internalUserProfile.getUserInfoMap() == null) ? new HashMap<>() : internalUserProfile.getUserInfoMap();
      boolean isModified = internalUserInfoMap.isEmpty();
      if (!isModified) {
        Set<String> externalUserProfileAttributes = externalUserInfoMap.keySet();
        for (String externalUserProfileAttribute : externalUserProfileAttributes) {
          isModified |= !internalUserInfoMap.containsKey(externalUserProfileAttribute)
              || !Objects.equals(externalUserInfoMap.get(externalUserProfileAttribute),
                                 internalUserInfoMap.get(externalUserProfileAttribute));
          if (isModified) {
            break;
          }
        }
      }

      if (isModified) {
        internalUserInfoMap.putAll(externalUserInfoMap);
        UserProfile userProfile = organizationService.getUserProfileHandler().createUserProfileInstance(username);
        userProfile.setUserInfoMap(internalUserInfoMap);
        organizationService.getUserProfileHandler().saveUserProfile(userProfile, true);
        return userProfile;
      }
    }
    return internalUserProfile;
  }

  private User importUser(String username, boolean deleted, boolean updateModified, boolean updateDeleted) throws Exception {
    User internalUser = organizationService.getUserHandler().findUserByName(username, UserStatus.ANY);
    if (deleted && internalUser != null) {
      // Do not delete user profile and memberships because it's already
      // deleted by previous statement
      LOG.trace("Remove from internal store deleted user '{}' from external store", username);
      organizationService.getUserHandler().removeUser(username, true);

      // Triggering event is optional, thus exceptions must be catched
      try {
        listenerService.broadcast(IDMExternalStoreService.USER_DELETED_FROM_EXTERNAL_STORE, this, internalUser);
      } catch (Exception e) {
        LOG.warn("Error while triggering event on user '" + username + "' data import (delete) from external store", e);
      }
      return internalUser;
    }

    boolean isNew = internalUser == null;

    boolean isModified = false;
    if (isNew) {
      User externalUser = externalStoreService.getEntity(IDMEntityType.USER, username);
      externalUser.setOriginatingStore(OrganizationService.EXTERNAL_STORE);

      // This is a mandatory treatment, thus it should not be surrounded by
      // try/catch block
      organizationService.getUserHandler().createUser(externalUser, true);
      internalUser = externalUser;
      try {
        // The user information creation listener triggering is optional, thus
        // this is surrounded by try/catch
        listenerService.broadcast(IDMExternalStoreService.USER_ADDED_FROM_EXTERNAL_STORE, this, externalUser);
      } catch (Exception e) {
        LOG.warn("Error while triggering event on user '" + username + "' data import (creation) from external store", e);
      }
      importEntityToInternalStore(IDMEntityType.USER_PROFILE, username, updateModified, updateDeleted);
    } else {
      if (updateModified) {
        isModified = updateModified && externalStoreService.isEntityModified(IDMEntityType.USER, username);
        if (isModified) {
          User externalUser = externalStoreService.getEntity(IDMEntityType.USER, username);
          mergeExternalToInternalUser(internalUser, externalUser);
          organizationService.getUserHandler().saveUser(internalUser, true);
          try {
            // The user information creation listener triggering is optional,
            // thus this is surrounded by try/catch
            listenerService.broadcast(IDMExternalStoreService.USER_MODIFIED_FROM_EXTERNAL_STORE, this, internalUser);
          } catch (Exception e) {
            LOG.warn("Error while triggering event on user '" + username + "' data import (modification) from external store", e);
          }
        }
        importEntityToInternalStore(IDMEntityType.USER_PROFILE, username, updateModified, updateDeleted);
      }
    }

    // Update user originating store on database without triggering
    // listeners because the field is for internal use only
    if (internalUser != null && internalUser.isInternalStore()) {
      internalUser.setOriginatingStore(OrganizationService.EXTERNAL_STORE);
      organizationService.getUserHandler().saveUser(internalUser, false);
    }

    return internalUser;
  }

  @SuppressWarnings("deprecation")
  private void mergeExternalToInternalUser(User internalUser, User externalUser) {
    if (StringUtils.isNotEmpty(externalUser.getEmail())) {
      internalUser.setEmail(externalUser.getEmail());
    }
    if (StringUtils.isNotEmpty(externalUser.getFirstName())) {
      internalUser.setFirstName(externalUser.getFirstName());
    }
    if (StringUtils.isNotEmpty(externalUser.getLastName())) {
      internalUser.setLastName(externalUser.getLastName());
    }
    if (StringUtils.isNotEmpty(externalUser.getDisplayName())) {
      internalUser.setDisplayName(externalUser.getDisplayName());
    }
    if (StringUtils.isNotEmpty(externalUser.getOrganizationId())) {
      internalUser.setOrganizationId(externalUser.getOrganizationId());
    }
    if (externalUser.getCreatedDate() != null) {
      internalUser.setCreatedDate(externalUser.getCreatedDate());
    }
    internalUser.setOriginatingStore(OrganizationService.EXTERNAL_STORE);
  }

  private List<IDMQueueEntry> processQueueEntries(List<IDMQueueEntry> queueEntries) throws Exception {
    List<IDMQueueEntry> treatedQueueEntries = new ArrayList<>();
    for (IDMQueueEntry queueEntry : queueEntries) {
      try {
        IDMEntityType<?> entityType = queueEntry.getEntityType();
        if (IDMOperationType.ADD_OR_UPDATE.equals(queueEntry.getOperationType())) {
          if (!externalStoreService.isEntityPresent(queueEntry.getEntityType(), queueEntry.getEntityId())) {
            // This could happen if the queue is processing an update on deleted
            // item from external store
            LOG.info("Entity of type '{}' with id '{}' was removed from external store before added/updated on internal store",
                     queueEntry.getEntityType().getName(),
                     queueEntry.getEntityId());
            treatedQueueEntries.add(queueEntry);
            continue;
          }
        }
        importEntityToInternalStore(queueEntry.getEntityType(), queueEntry.getEntityId(), true, true);

        // Process extra data in case of update operation only
        // If operationType = Delete, the related entities are already deleted
        // by IDM principal IDM removal
        if (IDMOperationType.ADD_OR_UPDATE.equals(queueEntry.getOperationType())) {
          if (entityType.equals(IDMEntityType.USER)) {
            importEntityToInternalStore(IDMEntityType.USER_MEMBERSHIPS, queueEntry.getEntityId(), true, true);
          } else if (entityType.equals(IDMEntityType.GROUP)) {
            importEntityToInternalStore(IDMEntityType.GROUP_MEMBERSHIPS, queueEntry.getEntityId(), true, true);
          }
        }

        treatedQueueEntries.add(queueEntry);
      } catch (Exception e) {
        idmQueueService.incrementRetry(Collections.singletonList(queueEntry));

        String entryId = queueEntry == null ? null : queueEntry.getEntityId();
        String entryType = queueEntry == null ? null : queueEntry.getEntityType().getClassType().getSimpleName();
        String operationType = queueEntry == null ? null : queueEntry.getOperationType().name();
        LOG.warn("Error while treating entity of type '" + entryType + "' with id '" + entryId + "' for operation type'"
            + operationType + "'", e);
      }
    }
    return treatedQueueEntries;
  }

  private void initializeDataImportScheduledJob() throws Exception {
    if (StringUtils.isBlank(scheduledDataImportJobCronExpression)) {
      LOG.warn("Can't initialize Cron Job for IDM Data import, the cron expression is empty");
      return;
    }
    InitParams params = new InitParams();
    PropertiesParam properties = new PropertiesParam();
    properties.setName("cronjob.info");
    properties.setProperty("jobName", "IDM.DATA.IMPORT");
    properties.setProperty("groupName", "PORTAL.IDM");
    properties.setProperty("job", IDMEntitiesImportJob.class.getName());
    properties.setProperty("expression", scheduledDataImportJobCronExpression);
    params.addParam(properties);
    jobSchedulerService.addCronJob(new CronJob(params));
  }

  private void initializeDataDeleteScheduledJob() throws Exception {
    if (StringUtils.isBlank(scheduledDataDeleteJobCronExpression)) {
      LOG.warn("Can't initialize Cron Job for IDM Data delete, the cron expression is empty");
      return;
    }
    InitParams params = new InitParams();
    PropertiesParam properties = new PropertiesParam();
    properties.setName("cronjob.info");
    properties.setProperty("jobName", "IDM.DATA.DELETE");
    properties.setProperty("groupName", "PORTAL.IDM");
    properties.setProperty("job", IDMEntitiesDeleteJob.class.getName());
    properties.setProperty("expression", scheduledDataDeleteJobCronExpression);
    params.addParam(properties);
    jobSchedulerService.addCronJob(new CronJob(params));
  }

  private void initializeQueueProcessingScheduledJob() throws Exception {
    if (StringUtils.isBlank(scheduledDataImportJobCronExpression)) {
      LOG.warn("Can't initialize Cron Job for IDM Queue Processing, the cron expression is empty");
      return;
    }
    InitParams params = new InitParams();
    PropertiesParam properties = new PropertiesParam();
    properties.setName("cronjob.info");
    properties.setProperty("jobName", "IDM.QUEUE");
    properties.setProperty("groupName", "PORTAL.IDM");
    properties.setProperty("job", IDMQueueProcessorJob.class.getName());
    properties.setProperty("expression", scheduledDataImportJobCronExpression);
    params.addParam(properties);
    jobSchedulerService.addCronJob(new CronJob(params));
  }

  /**
   * @return {@link LocalDateTime} relative to current date time on server
   */
  private LocalDateTime getLocalDateTime() {
    return ZonedDateTime.now(ZoneId.of("UTC")).toLocalDateTime();
  }

  private int forceCloseTransaction() {
    int i = 0;
    try {
      while (true) {
        RequestLifeCycle.end();
        i++;
      }
    } catch (Exception e) {
      // Expected when there is not transaction to close
    }
    return i;
  }

  private void openTransactionLevels(int numberOfTransactionLevels) {
    for (int i = 0; i < numberOfTransactionLevels; i++) {
      RequestLifeCycle.begin(container);
    }
  }

  private <T> void loadModifiedUsers(IDMEntityType<T> entityType,
                                     Consumer<String> modificationConsumer,
                                     LocalDateTime lastSuccessExecutionTime) throws Exception, InterruptedException {
    ListAccess<String> modifiedOrAddedUsers = externalStoreService.getAllOfType(IDMEntityType.USER, lastSuccessExecutionTime);
    if (modifiedOrAddedUsers == null) {
      return;
    }
    String[] usernames = modifiedOrAddedUsers.load(0, Integer.MAX_VALUE);
    for (String username : usernames) {
      if (interrupted) {
        throw new InterruptedException("Import of modified users check is interrupted, it will be retried next startup");
      }
      modificationConsumer.accept(username);
    }
  }

}
