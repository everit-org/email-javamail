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
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.util.Objects;

import javax.mail.MessagingException;

/**
 * Unchecked {@link MessagingException}.
 */
public class UncheckedMessagingException extends RuntimeException {

  private static final long serialVersionUID = 8389386874209750213L;

  public UncheckedMessagingException(final MessagingException cause) {
    super(Objects.requireNonNull(cause));
  }

  public UncheckedMessagingException(final String message, final MessagingException cause) {
    super(message, Objects.requireNonNull(cause));
  }

  /**
   * Called to read the object from a stream.
   *
   * @throws InvalidObjectException
   *           if the object is invalid or has a cause that is not an {@code IOException}
   */
  private void readObject(final ObjectInputStream s)
      throws IOException, ClassNotFoundException {
    s.defaultReadObject();
    Throwable cause = super.getCause();
    if (!(cause instanceof MessagingException)) {
      throw new InvalidObjectException("Cause must be an MessagingException");
    }
  }
}
