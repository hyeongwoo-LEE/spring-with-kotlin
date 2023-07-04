package com.group.libraryapp.service.user

import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.loanHistory.UserLoanHistory
import com.group.libraryapp.domain.user.loanHistory.UserLoanHistoryRepository
import com.group.libraryapp.domain.user.loanHistory.UserLoanStatus
import com.group.libraryapp.dto.user.request.UserCreateRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UserServiceTest @Autowired constructor(
        private val userRepository: UserRepository,
        private val userService: UserService,
        private val userLoanHistoryRepository: UserLoanHistoryRepository
) {
    @AfterEach
    fun clean() {
        userRepository.deleteAll()
    }

    @DisplayName("유저 저장이 정상 동작한다.")
    @Test
    fun saveUserTest() {
        // given
        val request = UserCreateRequest("최태현", null)

        // when
        userService.saveUser(request)

        // then
        var results = userRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].name).isEqualTo("최태현")
        assertThat(results[0].age).isNull()
    }
    
    @DisplayName("유저 삭제가 정상 동작한다.")
    @Test
    fun deleteUserTest() {
        // given
        userRepository.save(User("A", null))
        
        // when
        userService.deleteUser("A")
        
        // then
        assertThat(userRepository.findAll()).isEmpty()
    }
    
    @DisplayName("대출 기록이 없는 유저도 응답에 포함된다.")
    @Test
    fun getUserLoanHistoriesTest1() {
        // given
        userRepository.save(User("A", null))
        
        // when
        val results = userService.getUserLoanHistories()

        // then
        assertThat(results).hasSize(1)
        assertThat(results[0].name).isEqualTo("A")
        assertThat(results[0].books).isEmpty()
    }

    @DisplayName("대출 기록이 많은 유저의 융답이 정상 동작한다.")
    @Test
    fun getUserLoanHistoriesTest2() {
        // given
        val savedUser = userRepository.save(User("A", null))
        userLoanHistoryRepository.saveAll(listOf(
                UserLoanHistory.fixture(savedUser, "책1", UserLoanStatus.LOANED),
                UserLoanHistory.fixture(savedUser, "책2", UserLoanStatus.LOANED),
                UserLoanHistory.fixture(savedUser, "책3", UserLoanStatus.RETURNED),
        ))

        // when
        val results = userService.getUserLoanHistories()

        // then
        assertThat(results).hasSize(1)
        assertThat(results[0].name).isEqualTo("A")
        assertThat(results[0].books).extracting("isReturn")
                .containsExactlyInAnyOrder(false, false, true)
    }

}