package com.cask

import play.api.i18n.{I18NSupportLowPriorityImplicits, Messages, MessagesApi}
import play.api.mvc.RequestHeader

trait I18nSupport extends I18NSupportLowPriorityImplicits {
  def messagesApi: MessagesApi

  /**
   * Converts from a request directly into a Messages.
   *
   * @param request the incoming request
   * @return The preferred [[Messages]] according to the given [[play.api.mvc.RequestHeader]]
   */
  implicit def request2Messages(implicit request: RequestHeader): Messages = messagesApi.preferred(request)
}