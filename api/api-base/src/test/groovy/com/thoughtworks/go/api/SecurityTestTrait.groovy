/*
 * Copyright 2018 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thoughtworks.go.api

import com.thoughtworks.go.api.mocks.MockHttpServletResponseAssert
import com.thoughtworks.go.api.util.HaltMessages
import org.junit.jupiter.api.BeforeEach
import org.mockito.invocation.InvocationOnMock
import spark.Request
import spark.Response

import static org.mockito.Matchers.any
import static org.mockito.Mockito.*

trait SecurityTestTrait {

  def reachedControllerMessage

  @BeforeEach
  void stubControllerAction() {
    this.reachedControllerMessage = "REACHED_CONTROLLER_${UUID.randomUUID()}".toString()
    doAnswer({ InvocationOnMock invocation ->
      Request req = invocation.arguments.first()
      Response res = invocation.arguments.last()
      res.status(99999)
      return [message: reachedControllerMessage]
    }).when(controller)."${controllerMethodUnderTest}"(any() as Request, any() as Response)
  }

  abstract String getControllerMethodUnderTest()

  abstract void makeHttpCall()

  def assertRequestAuthorized() {
    verify(controller)."${controllerMethodUnderTest}"(any(), any())

    ((MockHttpServletResponseAssert) assertThatResponse())
      .hasStatus(99999)
      .hasContentType(controller.mimeType)
      .hasJsonBody([message: reachedControllerMessage])
  }

  def assertRequestNotAuthorized() {
    verify(controller, never())."${controllerMethodUnderTest}"(any(), any())

    ((MockHttpServletResponseAssert) assertThatResponse())
      .hasContentType(controller.mimeType)
      .hasStatus(401)
      .hasJsonMessage(HaltMessages.unauthorizedMessage())
  }

}