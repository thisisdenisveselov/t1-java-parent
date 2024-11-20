package ru.t1.java.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.t1.java.demo.model.dto.TransactionDto;
import ru.t1.java.demo.service.TransactionService;
import ru.t1.java.demo.util.TransactionMapper;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public TransactionDto createTransaction(@RequestBody TransactionDto transactionDto){
        return TransactionMapper.toDto(transactionService.createTransaction(TransactionMapper.toEntity(transactionDto)));
    }

    @GetMapping
    public List<TransactionDto> getTransactions(){
        return transactionService.getTransactions().stream()
                .map(TransactionMapper::toDto)
                .toList();
    }

    @GetMapping(value = "/{id}")
    public TransactionDto getTransactionById(@PathVariable Long id){
        return TransactionMapper.toDto(transactionService.getTransaction(id));
    }

    @PatchMapping("/{id}")
    public TransactionDto updateTransaction(@PathVariable Long id, @RequestBody TransactionDto transactionDto){
        return TransactionMapper.toDto(transactionService.updateTransaction(id, TransactionMapper.toEntity(transactionDto)));
    }

    @DeleteMapping("/{id}")
    public void deleteTransaction(@PathVariable Long id){
        transactionService.deleteTransaction(id);
    }
}
