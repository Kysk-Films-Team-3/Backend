package com.kyskfilms.service

import com.kyskfilms.dto.CreateUserDto
import com.kyskfilms.dto.UpdateUserDto
import com.kyskfilms.dto.UserDto
import com.kyskfilms.entity.User
import com.kyskfilms.exception.ResourceNotFoundException
import com.kyskfilms.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService(private val userRepository: UserRepository) {

    fun createUser(createUserDto: CreateUserDto): UserDto {
        if (userRepository.existsByEmail(createUserDto.email)) {
            throw IllegalArgumentException("User with email ${createUserDto.email} already exists")
        }

        val user = User(
            email = createUserDto.email,
            firstName = createUserDto.firstName,
            lastName = createUserDto.lastName,
            subscriptionType = createUserDto.subscriptionType
        )

        return userRepository.save(user).toDto()
    }

    @Transactional(readOnly = true)
    fun getUserById(id: Long): UserDto {
        val user = userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("User with id $id not found") }
        return user.toDto()
    }

    @Transactional(readOnly = true)
    fun getUserByEmail(email: String): UserDto {
        val user = userRepository.findByEmail(email)
            ?: throw ResourceNotFoundException("User with email $email not found")
        return user.toDto()
    }

    fun updateUser(id: Long, updateUserDto: UpdateUserDto): UserDto {
        val existingUser = userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("User with id $id not found") }

        // Вместо copy() обновляем поля напрямую (так как это обычный class, а не data class)
        updateUserDto.firstName?.let { existingUser.firstName = it }
        updateUserDto.lastName?.let { existingUser.lastName = it }
        updateUserDto.profilePicture?.let { existingUser.profilePicture = it }
        updateUserDto.subscriptionType?.let { existingUser.subscriptionType = it }

        return userRepository.save(existingUser).toDto()
    }

    fun deleteUser(id: Long) {
        if (!userRepository.existsById(id)) {
            throw ResourceNotFoundException("User with id $id not found")
        }
        userRepository.deleteById(id)
    }

    private fun User.toDto() = UserDto(
        id = id ?: throw IllegalStateException("User ID cannot be null after persistence"),
        email = email,
        firstName = firstName,
        lastName = lastName,
        profilePicture = profilePicture,
        subscriptionType = subscriptionType
    )
}