package com.martyphee.tempmonitor.model

import java.time.Instant

case class Reading(id: Long, tmp: Float, createdAt: Instant)
