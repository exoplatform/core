package org.exoplatform.services.organization.externalstore;

import java.time.LocalDateTime;
import java.util.List;

import org.picocontainer.Startable;

import org.exoplatform.container.*;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.management.annotations.*;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.management.rest.annotations.RESTEndpoint;
import org.exoplatform.services.organization.*;
import org.exoplatform.services.organization.externalstore.model.*;

/**
 * This class provides external store import and IDM queue operations via REST
 * and JMX MBean
 */
@Managed
@ManagedDescription("eXo Platform - IDM External Store")
@NameTemplate({ @Property(key = "service", value = "idm"), @Property(key = "name", value = "IDMExternalStoreService") })
@RESTEndpoint(path = "idm.externalstore")
public class IDMExternalStoreManagedBean implements Startable {

  private IDMExternalStoreService       externalStoreService;

  private OrganizationService           organizationService;

  private IDMExternalStoreImportService externalStoreImportService;

  private IDMQueueService               idmQueueService;

  public IDMExternalStoreManagedBean() {
  }

  /**
   * Retrieves the list of Users and Groups that have been modified or added on
   * external store and that aren't imported yet to internal store
   * 
   * @return
   * @throws Exception
   */
  @Managed
  @ManagedDescription("Returns modified entries on external store")
  @Impact(ImpactType.READ)
  public String listModifiedEntriesOnExternalStore() throws Exception {
    StringBuffer result = new StringBuffer();

    begin();
    try {
      getExternalStoreImportService().checkModifiedEntitiesOfType(IDMEntityType.USER, (externalUsername) -> {
        try {
          User internalStoreUser = getOrganizationService().getUserHandler().findUserByName(externalUsername, UserStatus.ANY);
          if (internalStoreUser == null) {
            result.append("User Added: ");
            result.append(externalUsername);
            result.append("\r\n");
          } else if (getExternalStoreService().isEntityModified(IDMEntityType.USER, externalUsername)) {
            result.append("User Modified: ");
            result.append(externalUsername);
            result.append("\r\n");
          }
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }, false);
      getExternalStoreImportService().checkModifiedEntitiesOfType(IDMEntityType.GROUP, (externalGroupId) -> {
        try {
          Group internalStoreGroup = getOrganizationService().getGroupHandler().findGroupById(externalGroupId);
          if (internalStoreGroup == null) {
            result.append("Group Added: ");
            result.append(externalGroupId);
            result.append("\r\n");
          } else {
            result.append("Group and/or memberships Modified: ");
            result.append(externalGroupId);
            result.append("\r\n");
          }
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }, false);
    } finally {
      end();
    }
    return result.toString();
  }

  /**
   * Retrieves the list of Users and Groups that have been deleted from external
   * store and that are existing on internal store
   * 
   * @return
   * @throws Exception
   */
  @Managed
  @ManagedDescription("Returns deleted entries from external store")
  @Impact(ImpactType.READ)
  public String listDeletedEntriesOnExternalStore() throws Exception {
    StringBuffer result = new StringBuffer();
    begin();
    try {
      getExternalStoreImportService().checkDeletedEntitiesOfType(IDMEntityType.USER, (externalUsername) -> {
        try {
          result.append("User Deleted: ");
          result.append(externalUsername);
          result.append("\r\n");
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
      getExternalStoreImportService().checkDeletedEntitiesOfType(IDMEntityType.GROUP, (externalGroupId) -> {
        try {
          result.append("Group Deleted: ");
          result.append(externalGroupId);
          result.append("\r\n");
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
    } finally {
      end();
    }
    return result.toString();
  }

  /**
   * Check modified entries and import them to IDM Queue
   * 
   * @throws Exception
   */
  @Managed
  @ManagedDescription("Check modified entries and import them to IDM Queue")
  @Impact(ImpactType.READ)
  public void importModifiedEntriesToQueue() throws Exception {
    begin();
    try {
      getExternalStoreImportService().importAllModifiedEntitiesToQueue();
    } finally {
      end();
    }
  }

  /**
   * Force Import all entities of type : 1 = User / 2 = Group / 3 =
   * MembershipType
   * 
   * @param entityType
   */
  @Managed
  @ManagedDescription("Force Import all entities of type : 1 = User / 2 = Group / 3 = MembershipType")
  @Impact(ImpactType.READ)
  public void forceImportAllEntitiesToQueue(@ManagedDescription("Entity type index, 1 = User / 2 = Group / 3 = MembershipType") @ManagedName("entityType") String entityType) {
    begin();
    try {
      getExternalStoreImportService().forceUpdateEntitiesOfType(IDMEntityType.getEntityType(Integer.parseInt(entityType)));
    } finally {
      end();
    }
  }

  /**
   * Force Import a specific entity of type : 1 = User / 2 = Group / 3 =
   * MembershipType
   * 
   * @param entityType
   * @param entityId
   * @throws Exception
   */
  @Managed
  @ManagedDescription("Force Import a specific entity identified by its id and type : 1 = User / 2 = Group / 3 = MembershipType")
  @Impact(ImpactType.READ)
  public void forceImportEntity(@ManagedDescription("Entity type index, 1 = User / 2 = Group / 3 = MembershipType") @ManagedName("entityType") String entityType,
                                @ManagedDescription("Entity id") @ManagedName("entityId") String entityId) throws Exception {
    begin();
    try {
      IDMEntityType<?> entityTypeObj = IDMEntityType.getEntityType(Integer.parseInt(entityType));
      getExternalStoreImportService().importEntityToInternalStore(entityTypeObj,
                                                                  entityId,
                                                                  true,
                                                                  true);
      if (IDMEntityType.GROUP.equals(entityTypeObj)) {
        getExternalStoreImportService().importEntityToInternalStore(IDMEntityType.GROUP_MEMBERSHIPS,
                                                                    entityId,
                                                                    true,
                                                                    true);
      }
    } finally {
      end();
    }
  }

  /**
   * Check deleted entries from external store, that exists on internal store
   * and import them to IDM Queue
   * 
   * @throws Exception
   */
  @Managed
  @ManagedDescription("Check deleted entries from external store, that exists on internal store and import them to IDM Queue")
  @Impact(ImpactType.READ)
  public void importDeletedEntriesToQueue() throws Exception {
    begin();
    try {
      getExternalStoreImportService().checkAllEntitiesToDeleteIntoQueue();
    } finally {
      end();
    }
  }

  /**
   * Purge processed queue entries
   * 
   * @throws Exception
   */
  @Managed
  @ManagedDescription("Purge processed queue entries")
  @Impact(ImpactType.READ)
  public void deleteProcessedEntriesFromQueue() throws Exception {
    getIdmQueueService().deleteProcessedEntries();
  }

  /**
   * Purge queue entries which number of retries has exceeded maxRetries
   * 
   * @throws Exception
   */
  @Managed
  @ManagedDescription("Purge queue entries which number of retries has exceeded maxRetries")
  @Impact(ImpactType.READ)
  public void deleteExceededRetriesEntriesFromQueue() throws Exception {
    getIdmQueueService().deleteExceededRetriesEntries();
  }

  /**
   * Process IDM Queue (delete, update and creation operations of IDM entities)
   */
  @Managed
  @ManagedDescription("Process IDM Queue: delete, update and creation operations of IDM entities")
  @Impact(ImpactType.READ)
  public void processQueue() {
    begin();
    try {
      getExternalStoreImportService().processQueueEntries();
    } finally {
      end();
    }
  }

  /**
   * Get current Queue size by counting only valid entries that aren't processed
   * yet and that have nbRetries less than maxRetries
   * 
   * @throws Exception
   */
  @Managed
  @ManagedDescription("Get current Queue size by counting only valid entries that aren't processed yet and that have nbRetries < maxRetries")
  @Impact(ImpactType.READ)
  public int countAllValidQueueEntries() throws Exception {
    begin();
    try {
      return getIdmQueueService().countAll();
    } finally {
      end();
    }
  }

  /**
   * Get Queue entries count corresponding to nbRetries
   * 
   * @param nbRetries
   * @throws Exception
   */
  @Managed
  @ManagedDescription("Get Queue entries count corresponding to nbRetries")
  @Impact(ImpactType.READ)
  public int countQueueEntries(@ManagedDescription("Number of retries of queue entries") @ManagedName("nbRetries") String nbRetries) throws Exception {
    begin();
    try {
      return getIdmQueueService().count(Integer.parseInt(nbRetries));
    } finally {
      end();
    }
  }

  /**
   * Get Queue entries count corresponding to nbRetries
   * 
   * @param nbRetries
   * @param limit
   * @throws Exception
   */
  @Managed
  @ManagedDescription("Get Queue entries count corresponding to nbRetries")
  @Impact(ImpactType.READ)
  public String displayQueueEntries(@ManagedDescription("Number of retries of queue entries") @ManagedName("nbRetries") String nbRetries,
                                    @ManagedDescription("Number of queue entries to display") @ManagedName("limit") String limit) throws Exception {
    StringBuilder result = new StringBuilder();
    begin();
    try {
      List<IDMQueueEntry> entries = getIdmQueueService().pop(Integer.parseInt(limit), Integer.parseInt(nbRetries), false);
      for (IDMQueueEntry idmQueueEntry : entries) {
        if (idmQueueEntry.getOperationType() == IDMOperationType.ADD_OR_UPDATE) {
          result.append("Add/Modify ");
        } else if (idmQueueEntry.getEntityType() == IDMEntityType.GROUP) {
          result.append("Delete ");
        }

        if (idmQueueEntry.getEntityType() == IDMEntityType.USER) {
          result.append("user '");
        } else if (idmQueueEntry.getEntityType() == IDMEntityType.GROUP) {
          result.append("group '");
        } else if (idmQueueEntry.getEntityType() == IDMEntityType.ROLE) {
          result.append("role '");
        }

        result.append(idmQueueEntry.getEntityId()).append("'");

        result.append("\n");
      }
    } finally {
      end();
    }
    return result.toString();
  }

  /**
   * Retrieves IDM Queue entries processing max retries
   */
  @Managed
  @ManagedDescription("Retrieves IDM Queue entries processing max retries")
  @Impact(ImpactType.READ)
  public int retrieveMaxRetries() {
    return getIdmQueueService().getMaxRetries();
  }

  /**
   * Get last checked time of EntityType: 1 = User / 2 = Group / 3 =
   * MembershipType
   * 
   * @param entityType
   * @throws Exception
   */
  @Managed
  @ManagedDescription("Get last checked time of EntityType: 1 = User / 2 = Group / 3 = MembershipType")
  @Impact(ImpactType.READ)
  public LocalDateTime retrieveLastCheckedTimeForType(@ManagedDescription("Entity type index, 1 = User / 2 = Group / 3 = MembershipType") @ManagedName("entityType") String entityType) throws Exception {
    begin();
    try {
      return getIdmQueueService().getLastCheckedTime(IDMEntityType.getEntityType(Integer.parseInt(entityType)));
    } finally {
      end();
    }
  }

  @Override
  public void start() {
  }

  @Override
  public void stop() {
  }

  private OrganizationService getOrganizationService() {
    if (organizationService == null) {
      organizationService = getService(OrganizationService.class);
    }
    return organizationService;
  }

  private IDMExternalStoreImportService getExternalStoreImportService() {
    if (externalStoreImportService == null) {
      externalStoreImportService = getService(IDMExternalStoreImportService.class);
    }
    return externalStoreImportService;
  }

  private IDMExternalStoreService getExternalStoreService() {
    if (externalStoreService == null) {
      externalStoreService = getService(IDMExternalStoreService.class);
    }
    return externalStoreService;
  }

  private IDMQueueService getIdmQueueService() {
    if (idmQueueService == null) {
      idmQueueService = getService(IDMQueueService.class);
    }
    return idmQueueService;
  }

  private static <T> T getService(Class<T> clazz) {
    return clazz.cast(getContainer().getComponentInstanceOfType(clazz));
  }

  private static ExoContainer getContainer() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    if (container instanceof RootContainer) {
      container = PortalContainer.getInstance();
    }
    return container;
  }

  private void begin() {
    ExoContainer container = getContainer();
    RequestLifeCycle.begin(container);
    if (container instanceof PortalContainer) {
      ClassLoader classLoader = ((PortalContainer) container).getPortalClassLoader();
      Thread.currentThread().setContextClassLoader(classLoader);
    }
  }

  private void end() {
    RequestLifeCycle.end();
  }

}
