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
package org.everit.email.javamail.sender;

import javax.mail.Session;

import org.everit.email.Email;
import org.everit.email.javamail.sender.internal.JavaMailBulkEmailSender;
import org.everit.email.sender.BulkEmailSender;
import org.everit.email.sender.EmailSender;

/**
 * Sends e-mails based on the JSR 919 API.
 */
public class JavaMailEmailSender implements EmailSender {

  private final Session session;

  public JavaMailEmailSender(final Session session) {
    this.session = session;
  }

  @Override
  public BulkEmailSender openBulkEmailSender() {
    return new JavaMailBulkEmailSender(session);
  }

  @Override
  public void sendEmail(final Email email) {
    try (BulkEmailSender bulkEmailSender = openBulkEmailSender()) {
      bulkEmailSender.sendEmail(email);
    }
  }

}
