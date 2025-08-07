package com.jotoai.voenix.shop.user.internal.repository

import com.jotoai.voenix.shop.user.api.dto.UserSearchCriteria
import com.jotoai.voenix.shop.user.internal.entity.Role
import com.jotoai.voenix.shop.user.internal.entity.User
import jakarta.persistence.criteria.Join
import jakarta.persistence.criteria.JoinType
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification

/**
 * Utility class for building JPA Specifications for User entity queries.
 */
object UserSpecifications {
    /**
     * Creates a specification based on search criteria.
     */
    fun fromCriteria(criteria: UserSearchCriteria): Specification<User> =
        Specification { root, query, criteriaBuilder ->
            val predicates = mutableListOf<Predicate>()

            // Soft delete filter (unless explicitly including deleted)
            if (!criteria.includeDeleted) {
                predicates.add(criteriaBuilder.isNull(root.get<Any>("deletedAt")))
            }

            // Email exact match
            criteria.email?.let { email ->
                predicates.add(criteriaBuilder.equal(root.get<String>("email"), email))
            }

            // Email contains
            criteria.emailContains?.let { emailContains ->
                predicates.add(
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")),
                        "%${emailContains.lowercase()}%",
                    ),
                )
            }

            // First name exact match
            criteria.firstName?.let { firstName ->
                predicates.add(criteriaBuilder.equal(root.get<String>("firstName"), firstName))
            }

            // First name contains
            criteria.firstNameContains?.let { firstNameContains ->
                predicates.add(
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("firstName")),
                        "%${firstNameContains.lowercase()}%",
                    ),
                )
            }

            // Last name exact match
            criteria.lastName?.let { lastName ->
                predicates.add(criteriaBuilder.equal(root.get<String>("lastName"), lastName))
            }

            // Last name contains
            criteria.lastNameContains?.let { lastNameContains ->
                predicates.add(
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("lastName")),
                        "%${lastNameContains.lowercase()}%",
                    ),
                )
            }

            // Phone number exact match
            criteria.phoneNumber?.let { phoneNumber ->
                predicates.add(criteriaBuilder.equal(root.get<String>("phoneNumber"), phoneNumber))
            }

            // User must have ALL specified roles
            criteria.roles?.takeIf { it.isNotEmpty() }?.let { roles ->
                val roleJoin: Join<User, Role> = root.join("roles", JoinType.INNER)
                val roleNamePredicate = roleJoin.get<String>("name").`in`(roles)
                predicates.add(roleNamePredicate)

                // Ensure user has ALL roles, not just ANY
                query?.groupBy(root.get<Long>("id"))
                query?.having(
                    criteriaBuilder.equal(
                        criteriaBuilder.countDistinct(roleJoin.get<String>("name")),
                        roles.size.toLong(),
                    ),
                )
            }

            // User must have AT LEAST ONE of the specified roles
            criteria.hasAnyRole?.takeIf { it.isNotEmpty() }?.let { anyRoles ->
                val roleJoin: Join<User, Role> = root.join("roles", JoinType.INNER)
                val anyRoleNamePredicate = roleJoin.get<String>("name").`in`(anyRoles)
                predicates.add(anyRoleNamePredicate)

                // Ensure distinct results when joining with roles
                query?.distinct(true)
            }

            // Combine all predicates with AND
            criteriaBuilder.and(*predicates.toTypedArray())
        }

    /**
     * Specification for active users only (not soft deleted).
     */
    fun isActive(): Specification<User> =
        Specification { root, _, criteriaBuilder ->
            criteriaBuilder.isNull(root.get<Any>("deletedAt"))
        }

    /**
     * Specification for users with specific roles.
     */
    fun hasRoles(roleNames: Set<String>): Specification<User> {
        return Specification { root, query, criteriaBuilder ->
            if (roleNames.isEmpty()) {
                return@Specification criteriaBuilder.conjunction()
            }

            val roleJoin: Join<User, Role> = root.join("roles", JoinType.INNER)
            val roleNamePredicate = roleJoin.get<String>("name").`in`(roleNames)

            // Ensure distinct results
            query?.distinct(true)

            roleNamePredicate
        }
    }

    /**
     * Specification for users with ANY of the specified roles.
     */
    fun hasAnyRole(roleNames: Set<String>): Specification<User> = hasRoles(roleNames)

    /**
     * Specification for users with ALL of the specified roles.
     */
    fun hasAllRoles(roleNames: Set<String>): Specification<User> {
        return Specification { root, query, criteriaBuilder ->
            if (roleNames.isEmpty()) {
                return@Specification criteriaBuilder.conjunction()
            }

            val roleJoin: Join<User, Role> = root.join("roles", JoinType.INNER)
            val roleNamePredicate = roleJoin.get<String>("name").`in`(roleNames)

            query?.groupBy(root.get<Long>("id"))
            query?.having(
                criteriaBuilder.equal(
                    criteriaBuilder.countDistinct(roleJoin.get<String>("name")),
                    roleNames.size.toLong(),
                ),
            )

            roleNamePredicate
        }
    }
}
