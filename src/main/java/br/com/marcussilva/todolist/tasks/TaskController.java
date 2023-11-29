package br.com.marcussilva.todolist.tasks;

import br.com.marcussilva.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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

@RestController
@RequestMapping("/tasks")
public class TaskController {

  @Autowired
  private ITaskRepository taskRepository;

  @PostMapping
  public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
    Object idUser = request.getAttribute("idUser");
    taskModel.setIdUser((UUID) idUser);

    LocalDateTime currentDate = LocalDateTime.now();
    if (currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Data(s) inválida(s)");
    }

    if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Data(s) inválida");
    }

    TaskModel task = this.taskRepository.save(taskModel);

    return ResponseEntity.status(HttpStatus.OK).body(task);
  }

  @GetMapping
  public List<TaskModel> list(HttpServletRequest request) {
    Object idUser = request.getAttribute("idUser");
    return this.taskRepository.findByIdUser((UUID) idUser);
  }

  @PutMapping("/{id}")
  public TaskModel update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id) {
    Object idUser = request.getAttribute("idUser");
    taskModel.setIdUser((UUID) idUser);

    TaskModel task = this.taskRepository.findById(id).orElse(null);

    Utils.copyNonNullProperties(taskModel, task);

    return this.taskRepository.save(taskModel);
  }
}
