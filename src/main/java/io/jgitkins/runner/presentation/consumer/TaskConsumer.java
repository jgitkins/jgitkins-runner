package io.jgitkins.runner.presentation.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskConsumer {

    @RabbitListener(queues = "jgitkins.task.queue")
    public void receiveTask(String message) {
        log.info("Received task message: {}", message);
        // TODO: Process the task
    }
}
