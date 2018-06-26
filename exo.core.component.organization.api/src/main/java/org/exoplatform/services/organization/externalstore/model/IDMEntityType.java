package org.exoplatform.services.organization.externalstore.model;

import java.util.*;

import org.exoplatform.services.organization.*;

/**
 * IDM entity types constants
 * 
 * @param <T>
 */
public class IDMEntityType<T> {

  public static final IDMEntityType<User>           USER              = new IDMEntityType<>("User", User.class);

  public static final IDMEntityType<UserProfile>    USER_PROFILE      = new IDMEntityType<>("UserProfile", UserProfile.class);

  public static final IDMEntityType<Group>          GROUP             = new IDMEntityType<>("Group", Group.class);

  public static final IDMEntityType<MembershipType> ROLE              = new IDMEntityType<>("MembershipType", MembershipType.class);

  public static final IDMEntityType<Membership>     MEMBERSHIP        = new IDMEntityType<>("Membership", Membership.class);

  @SuppressWarnings("rawtypes")
  public static final IDMEntityType<Set>            USER_MEMBERSHIPS  = new IDMEntityType<>("User memberships", Set.class);

  @SuppressWarnings("rawtypes")
  public static final IDMEntityType<List>           GROUP_MEMBERSHIPS = new IDMEntityType<>("Group memberships", List.class);

  private final Class<T>                         classType;

  private final int                              typeIndex;

  private final String                           name;

  public IDMEntityType(String name, Class<T> classType) {
    if (classType == null) {
      throw new IllegalArgumentException("classType parameter is mandatory");
    }
    if (name == null) {
      throw new IllegalArgumentException("entity type name parameter is mandatory");
    }
    this.classType = classType;
    this.name = name;
    if (this.classType.isAssignableFrom(User.class)) {
      typeIndex = 1;
    } else if (this.classType.isAssignableFrom(Group.class)) {
      typeIndex = 2;
    } else if (this.classType.isAssignableFrom(MembershipType.class)) {
      typeIndex = 3;
    } else if (this.classType.isAssignableFrom(Membership.class)) {
      typeIndex = 4;
    } else if (this.classType.isAssignableFrom(UserProfile.class)) {
      typeIndex = 5;
    } else if (this.classType.isAssignableFrom(Set.class)) {
      typeIndex = 6;
    } else if (this.classType.isAssignableFrom(List.class)) {
      typeIndex = 7;
    } else {
      typeIndex = 0;
    }
  }

  public Class<T> getClassType() {
    return classType;
  }

  public int getTypeIndex() {
    return typeIndex;
  }

  public String getName() {
    return name;
  }

  public static IDMEntityType<?> getEntityType(Object obj) {
    if (obj == null) {
      return null;
    } else if (obj instanceof User) {
      return USER;
    } else if (obj instanceof Group) {
      return GROUP;
    } else if (obj instanceof MembershipType) {
      return ROLE;
    } else if (obj instanceof Membership) {
      return MEMBERSHIP;
    } else if (obj instanceof UserProfile) {
      return USER_PROFILE;
    } else if (obj instanceof Set) {
      return USER_MEMBERSHIPS;
    } else if (obj instanceof List) {
      return GROUP_MEMBERSHIPS;
    }
    return null;
  }

  public static IDMEntityType<?> getEntityType(int typeIndex) {
    if (typeIndex == USER.getTypeIndex()) {
      return USER;
    } else if (typeIndex == GROUP.getTypeIndex()) {
      return GROUP;
    } else if (typeIndex == ROLE.getTypeIndex()) {
      return ROLE;
    } else if (typeIndex == MEMBERSHIP.getTypeIndex()) {
      return MEMBERSHIP;
    } else if (typeIndex == USER_PROFILE.getTypeIndex()) {
      return USER_PROFILE;
    } else if (typeIndex == USER_MEMBERSHIPS.getTypeIndex()) {
      return USER_MEMBERSHIPS;
    }
    return null;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof IDMEntityType)) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    IDMEntityType<?> other = (IDMEntityType<?>) obj;
    if (Objects.equals(this.getClassType(), other.getClassType())) {
      return true;
    }
    return false;
  }

  @Override
  public String toString() {
    return getClassType().getName();
  }

  @Override
  public int hashCode() {
    return classType == null ? 0 : classType.hashCode();
  }
}
