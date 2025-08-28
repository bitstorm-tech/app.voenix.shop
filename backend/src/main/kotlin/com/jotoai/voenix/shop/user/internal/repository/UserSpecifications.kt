package com.jotoai.voenix.shop.user.internal.repository

import com.jotoai.voenix.shop.user.api.dto.UserSearchCriteria
import com.jotoai.voenix.shop.user.internal.entity.Role
import com.jotoai.voenix.shop.user.internal.entity.User
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Join
import jakarta.persistence.criteria.JoinType
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
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

            addDeletedFilter(predicates, criteria, root, criteriaBuilder)
            addEmailPredicates(predicates, criteria, root, criteriaBuilder)
            addNamePredicates(predicates, criteria, root, criteriaBuilder)
            addPhonePredicates(predicates, criteria, root, criteriaBuilder)
            addRolePredicates(predicates, criteria, root, query, criteriaBuilder)

            when {
                predicates.isEmpty() -> criteriaBuilder.conjunction()
                predicates.size == 1 -> predicates.first()
                else -> predicates.reduce { acc, predicate -> criteriaBuilder.and(acc, predicate) }
            }
        }

    private fun addDeletedFilter(
        predicates: MutableList<Predicate>,
        criteria: UserSearchCriteria,
        root: Root<User>,
        criteriaBuilder: CriteriaBuilder,
    ) {
        if (!criteria.includeDeleted) {
            predicates.add(criteriaBuilder.isNull(root.get<Any>("deletedAt")))
        }
    }

    private fun addEmailPredicates(
        predicates: MutableList<Predicate>,
        criteria: UserSearchCriteria,
        root: Root<User>,
        criteriaBuilder: CriteriaBuilder,
    ) {
        criteria.email?.let { email ->
            predicates.add(criteriaBuilder.equal(root.get<String>("email"), email))
        }

        criteria.emailContains?.let { emailContains ->
            predicates.add(
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("email")),
                    "%${emailContains.lowercase()}%",
                ),
            )
        }
    }

    private fun addNamePredicates(
        predicates: MutableList<Predicate>,
        criteria: UserSearchCriteria,
        root: Root<User>,
        criteriaBuilder: CriteriaBuilder,
    ) {
        criteria.firstName?.let { firstName ->
            predicates.add(criteriaBuilder.equal(root.get<String>("firstName"), firstName))
        }

        criteria.firstNameContains?.let { firstNameContains ->
            predicates.add(
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("firstName")),
                    "%${firstNameContains.lowercase()}%",
                ),
            )
        }

        criteria.lastName?.let { lastName ->
            predicates.add(criteriaBuilder.equal(root.get<String>("lastName"), lastName))
        }

        criteria.lastNameContains?.let { lastNameContains ->
            predicates.add(
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("lastName")),
                    "%${lastNameContains.lowercase()}%",
                ),
            )
        }
    }

    private fun addPhonePredicates(
        predicates: MutableList<Predicate>,
        criteria: UserSearchCriteria,
        root: Root<User>,
        criteriaBuilder: CriteriaBuilder,
    ) {
        criteria.phoneNumber?.let { phoneNumber ->
            predicates.add(criteriaBuilder.equal(root.get<String>("phoneNumber"), phoneNumber))
        }
    }

    private fun addRolePredicates(
        predicates: MutableList<Predicate>,
        criteria: UserSearchCriteria,
        root: Root<User>,
        query: CriteriaQuery<*>?,
        criteriaBuilder: CriteriaBuilder,
    ) {
        criteria.roles?.takeIf { it.isNotEmpty() }?.let { roles ->
            val roleJoin: Join<User, Role> = root.join("roles", JoinType.INNER)
            val roleNamePredicate = roleJoin.get<String>("name").`in`(roles)
            predicates.add(roleNamePredicate)

            query?.groupBy(root.get<Long>("id"))
            query?.having(
                criteriaBuilder.equal(
                    criteriaBuilder.countDistinct(roleJoin.get<String>("name")),
                    roles.size.toLong(),
                ),
            )
        }

        criteria.hasAnyRole?.takeIf { it.isNotEmpty() }?.let { anyRoles ->
            val roleJoin: Join<User, Role> = root.join("roles", JoinType.INNER)
            val anyRoleNamePredicate = roleJoin.get<String>("name").`in`(anyRoles)
            predicates.add(anyRoleNamePredicate)

            query?.distinct(true)
        }
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
