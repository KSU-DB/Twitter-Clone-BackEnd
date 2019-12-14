package me.dblab.twitterclone.account;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.dblab.twitterclone.config.jwt.Jwt;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class AccountController {

    private final AccountService accountService;
    private final AccountValidator accountValidator;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Account> getAccount(@PathVariable String id) {
        return accountService.getAccount(id);
    }

    @PostMapping
    public Mono<ResponseEntity> saveAccount(@RequestBody AccountDto accountDto)  {
        this.validate(accountDto);
        return accountService.saveAccount(accountDto);
    }

    private void validate(AccountDto accountDto) {
        Errors errors = new BeanPropertyBindingResult(accountDto, "account");
        accountValidator.validate(accountDto, errors);
        if (errors.hasErrors()) {
            throw new ServerWebInputException(errors.toString());
        }
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<Jwt>> login(@RequestBody Account account) {
        return accountService.login(account);
    }

    // Update User
    @PutMapping("/{id}")
    public Mono<ResponseEntity> updateAccount(@PathVariable String id, @RequestBody AccountDto accountDto) {
        this.validate(accountDto);
        return accountService.updateAccount(id, accountDto);
    }

    // Delete User
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteAccount(@PathVariable String id)  {
        return accountService.deleteAccount(id);
    }
}

