package br.dev.igorcardoso.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.dev.igorcardoso.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("tasks")
public class TaskController {

  @Autowired
  private ITaskRepository taskRepository;

  @PostMapping("/")
  public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
    UUID userId = (UUID) request.getAttribute("userId");
    taskModel.setUserId(userId);

    LocalDateTime currentDate = LocalDateTime.now();

    if (currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de íncio/terminio deve ser maior do que atual");
    }

    if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("A data de íncio deve ser menor do que a data de terminio.");
    }

    TaskModel taskCreated = this.taskRepository.save(taskModel);
    return ResponseEntity.status(HttpStatus.OK).body(taskCreated);
  }

  @GetMapping("/")
  public List<TaskModel> list(HttpServletRequest request) {
    UUID userId = (UUID) request.getAttribute("userId");

    List<TaskModel> tasksModel = this.taskRepository.findByUserId(userId);

    return tasksModel;
  }

  @PutMapping("/{id}")
  public ResponseEntity update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id) {
    UUID userId = (UUID) request.getAttribute("userId");

    var task = this.taskRepository.findById(id).orElse(null);

    if (task == null) {
      return ResponseEntity.badRequest().body("Tarefa não pertence ao usuário.");
    }

    if (!task.getUserId().equals((userId))) {
      return ResponseEntity.badRequest().body("Tarefa não pertence ao usuário.");
    }

    Utils.copyNonNullProperties(taskModel, task);

    TaskModel taskUpdated = this.taskRepository.save(task);

    return ResponseEntity.status(HttpStatus.OK).body(taskUpdated);
  }
}
