package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.demo.entity.Task;
import com.example.demo.entity.User;
import com.example.demo.entity.enums.TaskStatus;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @EntityGraph(attributePaths = {"category"})
    List<Task> findByUserAndStatusOrderByDeadlineAsc(User user, TaskStatus status);

    @EntityGraph(attributePaths = {"category"})
    List<Task> findByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT t FROM Task t WHERE t.status = :status AND t.deadline BETWEEN :start AND :end AND t.reminderSent = false")
    List<Task> findByStatusAndDeadlineBetweenAndReminderNotSent(@Param("status") TaskStatus status,
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Modifying
    @Query("UPDATE Task t SET t.status = 'COMPLETED', t.completedAt = CURRENT_TIMESTAMP WHERE t.id = :taskId AND t.user.id = :userId AND t.status = 'ACTIVE'")
    int markCompletedIfOwned(@Param("taskId") Long taskId, @Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Task t SET t.reminderSent = true WHERE t.id = :taskId AND t.reminderSent = false")
    int markReminderSent(@Param("taskId") Long taskId);
}
