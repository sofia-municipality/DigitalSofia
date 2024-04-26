/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.models.common

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.digital.sofia.R

data class Message(
    val messageId: String? = null,
    val title: StringSource? = null,
    val message: StringSource,
    @DrawableRes val icon: Int? = R.drawable.ic_error,
    val state: State = State.ERROR,
    val gravity: Gravity = Gravity.START,
    val type: Type = Type.MESSAGE,
    val positiveButtonText: StringSource? = null,
    val negativeButtonText: StringSource? = null,
) {

    enum class State {
        ERROR,
        SUCCESS
    }

    enum class Gravity {
        START,
        CENTER
    }

    enum class Type {
        MESSAGE,
        ALERT
    }

    companion object {
        /**
         * Red error banners with [message] and error [icon] and gravity start.
         */
        fun error(
            message: String,
            @DrawableRes icon: Int = R.drawable.ic_error
        ): Message {
            return Message(
                message = StringSource.Text(message),
                icon = icon,
                state = State.ERROR
            )
        }

        fun error(
            @StringRes message: Int,
            @DrawableRes icon: Int = R.drawable.ic_error
        ): Message {
            return Message(
                message = StringSource.Res(message),
                icon = icon,
                state = State.ERROR
            )
        }

        /**
         * Green success banners with [message] and success [icon] and gravity start.
         */
        fun success(
            message: String,
            @DrawableRes icon: Int = R.drawable.ic_success
        ): Message {
            return Message(
                message = StringSource.Text(message),
                icon = icon,
                state = State.SUCCESS
            )
        }

        fun success(
            @StringRes message: Int,
            @DrawableRes icon: Int = R.drawable.ic_success
        ): Message {
            return Message(
                message = StringSource.Res(message),
                icon = icon,
                state = State.SUCCESS
            )
        }

        /**
         * Green success banners with simple [message] in the center of the banner.
         */
        fun successCenter(message: String): Message {
            return Message(
                message = StringSource.Text(message),
                icon = null,
                state = State.SUCCESS,
                gravity = Gravity.CENTER
            )
        }

        fun successCenter(@StringRes message: Int): Message {
            return Message(
                message = StringSource.Res(message),
                icon = null,
                state = State.SUCCESS,
                gravity = Gravity.CENTER
            )
        }
    }
}