package ru.practicum.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.category.controller.CategoryAdminController;
import ru.practicum.category.controller.CategoryPublicController;
import ru.practicum.event.controller.EventAdminController;
import ru.practicum.event.controller.EventPublicController;
import ru.practicum.request.controller.RequestController;
import ru.practicum.user.controller.UserController;

import javax.validation.ConstraintViolationException;

@RestControllerAdvice(assignableTypes = {
        UserController.class,
        CategoryAdminController.class,
        CategoryPublicController.class,
        RequestController.class,
        EventAdminController.class,
        EventPublicController.class
})
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handle(final EWMElementNotFoundException e) {
        return new ErrorResponse("Not found error: ", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handle(final EWMUpdateForbiddenException e) {
        return new ErrorResponse("Update forbidden error: ", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handle(final EWMRequestConfirmForbiddenException e) {
        return new ErrorResponse("Update request status error: ", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handle(final EWMConflictException e) {
        return new ErrorResponse("Update request status error: ", e.getMessage());
    }

    @ExceptionHandler({ConstraintViolationException.class,
                        MethodArgumentNotValidException.class,
                        MissingPathVariableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handle(Exception e) throws Exception {
        if (e instanceof ConstraintViolationException || e instanceof  MethodArgumentNotValidException || e instanceof MissingPathVariableException) {
            return new ErrorResponse("Validation error: ", e.getMessage());
        }
        throw e;
    }

}
