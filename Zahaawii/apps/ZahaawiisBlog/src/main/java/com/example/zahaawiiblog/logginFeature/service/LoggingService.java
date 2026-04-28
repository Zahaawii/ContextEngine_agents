package com.example.zahaawiiblog.logginFeature.service;

import com.example.zahaawiiblog.logginFeature.entity.Logging;
import com.example.zahaawiiblog.logginFeature.repository.LoggingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LoggingService {

    private final LoggingRepository loggingRepository;

    public LoggingService(LoggingRepository loggingRepository) {
        this.loggingRepository = loggingRepository;
    }

    public void log(Long userId, String test, String username, Long postId) {
        Logging logging = new Logging();
        logging.setUserId(userId);
        logging.setUsername(username);
        logging.setDescription(test);
        logging.setActionId(postId);
        logging.setLocalDateTime(LocalDateTime.now());
        loggingRepository.save(logging);
    }

}
