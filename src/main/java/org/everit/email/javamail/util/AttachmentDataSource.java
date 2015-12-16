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
package org.everit.email.javamail.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import org.everit.email.Attachment;

/**
 * Wraps an email {@link Attachment} and implements DataSource interface on the top of it.
 */
public class AttachmentDataSource implements DataSource {

  private final Attachment attachment;

  public AttachmentDataSource(final Attachment attachment) {
    this.attachment = attachment;
  }

  @Override
  public String getContentType() {
    return attachment.contentType;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return attachment.inputStreamSupplier.getStream();
  }

  @Override
  public String getName() {
    return attachment.name;
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    throw new UnsupportedOperationException();
  }

}
