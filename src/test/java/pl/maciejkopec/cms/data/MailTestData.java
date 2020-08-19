package pl.maciejkopec.cms.data;

import pl.maciejkopec.cms.dto.Mail;

public class MailTestData {

  public static Mail valid() {
    return new Mail("from@mail.com", "Name", "Message", "token");
  }

  public static Mail invalid() {
    return new Mail("", "", "", "");
  }
}
