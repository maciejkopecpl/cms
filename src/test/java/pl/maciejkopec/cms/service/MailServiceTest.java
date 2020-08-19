package pl.maciejkopec.cms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static pl.maciejkopec.cms.data.MailTestData.valid;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class MailServiceTest {

  @InjectMocks private MailService mailService;
  @Mock private JavaMailSender javaMailSender;

  @Test
  void shouldSendEmail() {
    final var result = mailService.send(valid()).block();

    assertThat(result).isEqualTo(valid());
  }

  @Test
  void shouldNotSendEmail() {
    doThrow(new RuntimeException()).when(javaMailSender).send(any(SimpleMailMessage.class));

    Assertions.assertThrows(RuntimeException.class, () -> mailService.send(valid()).block());
  }
}
