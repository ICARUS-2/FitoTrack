/*
 * Copyright (c) 2021 Jannis Scheibe <jannis@tadris.de>
 *
 * This file is part of FitoTrack
 *
 * FitoTrack is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     FitoTrack is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.tadris.fitness.recording.information

import android.content.Context
import de.tadris.fitness.R
import de.tadris.fitness.recording.gps.GpsWorkoutRecorder

class CurrentSpeed(context: Context) : GpsRecordingInformation(context) {

    override val id = "current_speed"
    override val isEnabledByDefault = true
    override fun canBeDisplayed() = true

    override fun getTitle(): String {
        return getString(R.string.currentSpeed)
    }

    override fun getDisplayedText(recorder: GpsWorkoutRecorder): String {
        return distanceUnitUtils.getSpeed(recorder.currentSpeed)
    }

    override fun getSpokenText(recorder: GpsWorkoutRecorder): String {
        return getString(R.string.currentSpeed) + ": " + distanceUnitUtils.getSpeed(
            recorder.currentSpeed,
            true
        ) + "."
    }
}