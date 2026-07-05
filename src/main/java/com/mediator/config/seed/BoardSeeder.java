package com.mediator.config.seed;

import com.mediator.master.entity.Board;
import com.mediator.master.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(1)
public class BoardSeeder implements CommandLineRunner {

    private final BoardRepository boardRepository;

    @Override
    public void run(String... args) {

        createBoard("CBSE");
        createBoard("ICSE");
        createBoard("ISC");
        createBoard("State Board");
        createBoard("NIOS");
        createBoard("IB");
        createBoard("IGCSE");
    }

    private void createBoard(String boardName) {

        if (!boardRepository.existsByName(boardName)) {

            boardRepository.save(
                    Board.builder()
                            .name(boardName)
                            .build());
        }
    }
}
