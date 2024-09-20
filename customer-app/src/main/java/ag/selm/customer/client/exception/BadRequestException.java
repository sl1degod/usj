package ag.selm.customer.client.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BadRequestException extends RuntimeException {

    private final List<String> errors;

    public BadRequestException(Throwable cause, List<String> errors) {
        super(cause);
        this.errors = errors;
    }
}
