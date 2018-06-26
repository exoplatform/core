package org.exoplatform.services.organization.externalstore;

import static java.time.temporal.ChronoField.*;

import java.time.LocalDateTime;
import java.time.format.*;
import java.util.Set;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.externalstore.model.IDMEntityType;

/**
 * This interface provides operations to implement for External Store API
 * Management. It manages IDM entities: Role (Membership type), User (and User
 * Profile), Group and Membership
 */
public interface IDMExternalStoreService {

  public static final String            UPDATE_USER_ON_LOGIN_PARAM              = "update.user.onlogin";

  /**
   * This search query is used for requesting
   */
  public static final DateTimeFormatter LDAP_MODIFICATION_DATE_FORMAT           = new DateTimeFormatterBuilder()
                                                                                                                .appendValue(YEAR,
                                                                                                                             4,
                                                                                                                             10,
                                                                                                                             SignStyle.NEVER)
                                                                                                                .appendValue(MONTH_OF_YEAR,
                                                                                                                             2)
                                                                                                                .appendValue(DAY_OF_MONTH,
                                                                                                                             2)
                                                                                                                .appendValue(HOUR_OF_DAY,
                                                                                                                             2)
                                                                                                                .appendValue(MINUTE_OF_HOUR,
                                                                                                                             2)
                                                                                                                .appendValue(SECOND_OF_MINUTE,
                                                                                                                             2)
                                                                                                                .toFormatter();

  public static final String            USER_AUTHENTICATED_USING_EXTERNAL_STORE = "exo.idm.externalStore.user.authenticated";

  public static final String            USER_DELETED_FROM_EXTERNAL_STORE        = "exo.idm.externalStore.user.deleted";

  public static final String            USER_ADDED_FROM_EXTERNAL_STORE          = "exo.idm.externalStore.user.new";

  public static final String            USER_MODIFIED_FROM_EXTERNAL_STORE       = "exo.idm.externalStore.user.modified";

  public static final String            GROUP_DELETED_FROM_EXTERNAL_STORE       = "exo.idm.externalStore.group.deleted";

  public static final String            GROUP_ADDED_FROM_EXTERNAL_STORE         = "exo.idm.externalStore.group.new";

  public static final String            GROUP_MODIFIED_FROM_EXTERNAL_STORE      = "exo.idm.externalStore.group.modified";

  /**
   * Authenticates user using external store only
   * 
   * @param username
   * @param password
   * @return
   * @throws Exception
   */
  boolean authenticate(String username, String password) throws Exception;

  /**
   * Get all entities of a chosen type from external store only which are
   * modified since a date. If the date is null, it will retrieve all entities
   * of chosen type.
   * 
   * @param entityType
   * @param sinceLastModified
   * @return
   * @throws Exception
   */
  ListAccess<String> getAllOfType(IDMEntityType<?> entityType, LocalDateTime sinceLastModified) throws Exception;

  /**
   * @return list access of internal users
   * 
   * @throws Exception
   */
  default ListAccess<User> getAllInternalUsers() throws Exception {
    return getOrganizationService().getUserHandler().findAllUsers();
  }

  /**
   * Get an entity identified by its id from external store
   * 
   * @param entityType
   * @param entityId
   * @return
   * @throws Exception
   */
  <T> T getEntity(IDMEntityType<T> entityType, Object entityId) throws Exception;

  /**
   * Check if entity of chosen type is modified on external store comparing to
   * what is stored on internal store
   * 
   * @param entityType
   * @param username
   * @return
   * @throws Exception
   */
  boolean isEntityModified(IDMEntityType<?> entityType, String username) throws Exception;

  /**
   * Check if an entity identified by its id is present in external store
   * 
   * @param entityType
   * @param entityId
   * @return
   * @throws Exception
   */
  default boolean isEntityPresent(IDMEntityType<?> entityType, Object entityId) throws Exception {
    return getEntity(entityType, entityId) != null;
  }

  /**
   * Get all entity types managed by external store
   * 
   * @return
   */
  Set<IDMEntityType<?>> getManagedEntityTypes();

  /**
   * Checks if the external store API is enabled
   * 
   * @return
   */
  boolean isEnabled();

  /**
   * If true, the user information will be updated on internal store if modified
   * on external store including User Profile and memberships
   * 
   * @return
   */
  boolean isUpdateInformationOnLogin();

  /**
   * Get Organization Service
   */
  OrganizationService getOrganizationService();

}
