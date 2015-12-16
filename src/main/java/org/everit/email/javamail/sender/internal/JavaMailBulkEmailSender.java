/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.email.javamail.sender.internal;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;

import org.everit.email.Email;
import org.everit.email.javamail.util.MessageGenerator;
import org.everit.email.javamail.util.UncheckedMessagingException;
import org.everit.email.sender.BulkEmailSender;

/**
 * Bulk email sender implementation.
 */
public class JavaMailBulkEmailSender implements BulkEmailSender {

  private final MessageGenerator messageGenerator;

  private Transport transport;

  /**
   * Constructor that opens a new Transport.
   *
   * @param session
   *          The session that is used to open the Transport.
   */
  public JavaMailBulkEmailSender(final Session session) {
    try {
      transport = session.getTransport();
      transport.connect();
    } catch (MessagingException e) {
      throw new UncheckedMessagingException(e);
    }
    messageGenerator = new MessageGenerator();
  }

  @Override
  public void close() {
    try {
      transport.close();
    } catch (MessagingException e) {
      throw new UncheckedMessagingException(e);
    }
  }

  @Override
  public void sendEmail(final Email email) {
    Message message = messageGenerator.generateMessage(email);
    try {
      transport.sendMessage(message, message.getAllRecipients());
    } catch (MessagingException e) {
      throw new UncheckedMessagingException(e);
    }
  }

}
