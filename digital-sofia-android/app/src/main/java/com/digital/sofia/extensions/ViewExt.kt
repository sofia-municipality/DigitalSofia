/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.extensions

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.*
import android.text.*
import android.text.style.UnderlineSpan
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.annotation.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.Insets
import androidx.core.text.HtmlCompat
import androidx.core.view.*
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty.COLOR_FILTER
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.model.KeyPath
import com.digital.sofia.R
import com.digital.sofia.models.common.StringSource
import com.google.android.material.animation.ArgbEvaluatorCompat
import java.lang.reflect.Method
import kotlin.math.abs
import kotlin.math.round

@Suppress("DEPRECATION")
fun AppCompatActivity.makeStatusBarTransparent(isLight: Boolean) {
    window.apply {
        clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        var flags = window.decorView.systemUiVisibility
        if (isLight) {
            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            flags = flags xor View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or flags
        statusBarColor = Color.TRANSPARENT
        navigationBarColor = Color.TRANSPARENT
    }
}

@Suppress("DEPRECATION")
fun Dialog.setStatusBarColor(@ColorRes color: Int) {
    val realColor = context.color(color)
    window?.let { window ->
        var flags = window.decorView.systemUiVisibility
        if (!isDark(realColor)) {
            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            flags = flags xor View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        window.decorView.systemUiVisibility = flags
        window.statusBarColor = realColor
    }
}

/**
 * Because we have a lot of fragments here, the first fragment would catch insets and
 * consume them. So we need manually to throw insets to all fragments (not only the first)
 * one by one. And then they will decide to use insets or not.
 *
 * This is because of [android:fitsSystemWindows] flag behavior. It consumes all
 * insets and do not spread them further or not even for his neighbours by hierarchy.
 * If two fragment in view pager for example, only the first one will get insets.
 *
 * param this - a container of fragments or insets catcher
 */
@Suppress("DEPRECATION")
fun ViewGroup.throwContainerInsetsFurther() {
    setOnApplyWindowInsetsListener { view, insets ->
        var consumed = false
        (view as ViewGroup).forEach { child ->
            val childResult = child.dispatchApplyWindowInsets(insets)
            // If the child consumed the insets, record it
            if (childResult.isConsumed) {
                consumed = true
            }
        }

        // If any of the children consumed the insets, return
        // an appropriate value
        if (consumed) insets.consumeSystemWindowInsets() else insets
    }
    ViewCompat.requestApplyInsets(this)
}

/**
 * The same as [throwContainerInsetsFurther] but throws insets to
 * [receivers] view groups children.
 */
@Suppress("DEPRECATION")
fun ViewGroup.throwContainerInsetsToReceiversChildren(vararg receivers: ViewGroup) {
    setOnApplyWindowInsetsListener { _, insets ->
        var consumed = false
        receivers.forEach {
            it.forEach { child ->
                val childResult = child.dispatchApplyWindowInsets(insets)
                // If the child consumed the insets, record it
                if (childResult.isConsumed) {
                    consumed = true
                }
            }
        }

        // If any of the children consumed the insets, return
        // an appropriate value
        if (consumed) insets.consumeSystemWindowInsets() else insets
    }
    ViewCompat.requestApplyInsets(this)
}

/**
 * The same as [throwContainerInsetsFurther] but throws insets using
 * [receivers] view groups.
 */
@Suppress("DEPRECATION")
fun ViewGroup.throwContainerInsetsToReceivers(vararg receivers: ViewGroup) {
    setOnApplyWindowInsetsListener { _, insets ->
        var consumed = false
        receivers.forEach {
            val result = it.dispatchApplyWindowInsets(insets)
            // If the receiver consumed the insets, record it
            if (result.isConsumed) {
                consumed = true
            }
        }

        // If any of the children consumed the insets, return
        // an appropriate value
        if (consumed) insets.consumeSystemWindowInsets() else insets
    }
    ViewCompat.requestApplyInsets(this)
}

/**
 * Removes navigation bar insets. Useful when we have bottom navigation
 * and it handles navigation bar insets, so children should not do that.
 *
 * param this - a container of fragments or insets catcher
 */
fun ViewGroup.applyInsetsWithoutNavigationBar() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val corrected = WindowInsetsCompat.Builder(insets)
            .setInsets(WindowInsetsCompat.Type.navigationBars(), Insets.NONE)
            .build()
        view.onApplyWindowInsets(corrected.toWindowInsets())
        WindowInsetsCompat.CONSUMED
    }
    ViewCompat.requestApplyInsets(this)
}

fun Context.toast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun isDark(@ColorInt color: Int): Boolean {
    return ColorUtils.calculateLuminance(color) < 0.5
}

fun Context.dpToPx(valueInDp: Float): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        valueInDp,
        resources.displayMetrics
    )
}

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun ImageView.tintRes(@ColorRes colorRes: Int) {
    setColorFilter(
        context.color(colorRes),
        PorterDuff.Mode.SRC_IN
    )
}

fun ImageView.tintColor(@ColorInt color: Int) {
    setColorFilter(color, PorterDuff.Mode.SRC_IN)
}

fun AppCompatImageView.tintRes(@ColorRes colorRes: Int) {
    tintParsed(context.color(colorRes))
}

fun ImageView.tintParsed(@ColorRes colorRes: Int) {
    setColorFilter(colorRes, PorterDuff.Mode.SRC_IN)
}

fun View.backgroundColor(@ColorRes colorRes: Int) {
    setBackgroundColor(context.color(colorRes))
}

fun View.setAlphaByOffset(
    incomingOffset: Int,
    maxOffset: Int,
    fraction: Float = 0.2f
) {
    val offset = abs(incomingOffset)
    val minOffset = maxOffset * (1f - fraction)
    alpha = if (offset < minOffset) {
        0f
    } else if (offset >= maxOffset) {
        1f
    } else {
        (offset - minOffset) / (maxOffset - minOffset)
    }
}

fun Context.color(@ColorRes colorRes: Int) = ContextCompat.getColor(this, colorRes)

fun Context.drawable(@DrawableRes drawableRes: Int) = ContextCompat.getDrawable(this, drawableRes)

fun Context.drawableByStyle(@DrawableRes drawableRes: Int, @StyleRes styleRes: Int): Drawable? {
    return ContextThemeWrapper(this, styleRes).let { themeWrapper ->
        ResourcesCompat.getDrawable(resources, drawableRes, themeWrapper.theme)
    }
}

fun Context?.pxDimen(@DimenRes dimenRes: Int): Int {
    return this?.resources?.getDimensionPixelSize(dimenRes) ?: 0
}

fun Context?.dpDimen(@DimenRes dimenRes: Int): Float {
    return if (this != null) {
        pxDimen(dimenRes) / resources.displayMetrics.density
    } else 0f
}

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View =
    LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)

/**
 * Get action bar height from device theme or just take the backup
 * height when the device has no actionBarSize attribute.
 */
fun View?.fetchActionBarHeight(): Int {
    val tv = TypedValue()
    var actionBarHeight = 0
    if (this != null) {
        actionBarHeight = if (context.theme.resolveAttribute(
                android.R.attr.actionBarSize, tv,
                true
            ) && !isInEditMode
        ) {
            TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
        } else {
            30
//            context.pxDimen(R.dimen.unused_action_bar_size)
        }
    }
    return actionBarHeight
}

/**
 * Ignore the fast series of clicks to prevent multiple action calls.
 *
 * @param call - a click listener
 * @return disposable of rx click method. Should be disposed with view destructor
 */
inline fun View.onClickThrottle(delay: Long = 500L, crossinline call: () -> Unit) {
    setOnClickListener(object : View.OnClickListener {
        var time = 0L
        override fun onClick(v: View?) {
            val newTime = System.currentTimeMillis()
            if (newTime - time > delay) {
                call.invoke()
                time = newTime
            }
        }
    })
}

inline fun View.onClick(crossinline call: () -> Unit) {
    setOnClickListener {
        call.invoke()
    }
}

/**
 * This method helps to remove the top overscroll effect from the recycler view.
 */
fun RecyclerView.removeTopOverscroll() {
    this.edgeEffectFactory = object : RecyclerView.EdgeEffectFactory() {
        override fun createEdgeEffect(view: RecyclerView, direction: Int): EdgeEffect {
            return when (direction) {
                DIRECTION_TOP -> object : EdgeEffect(view.context) {
                    // Disable default method behaviour
                    override fun onAbsorb(velocity: Int) {}

                    override fun onPull(deltaDistance: Float, displacement: Float) {}
                }

                else -> super.createEdgeEffect(view, direction)
            }
        }
    }
}

/**
 * Disable swipe refresh [this] layout when we scroll too far from top.
 * This code will fix the bottom recycler view [rv] glow.
 */
fun SwipeRefreshLayout.fixRecyclerViewGlow(rv: RecyclerView) {
    val layoutManager = rv.layoutManager as LinearLayoutManager
    val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
    isEnabled = (firstVisiblePosition == 0)
}

fun View?.findSuitableParent(): ViewGroup? {
    var view = this
    var fallback: ViewGroup? = null
    do {
        if (view is CoordinatorLayout) {
            // We've found a CoordinatorLayout, use it
            return view
        } else if (view is FrameLayout) {
            if (view.id == android.R.id.content) {
                // If we've hit the decor content view, then we didn't find a CoL in the
                // hierarchy, so use it.
                return view
            } else {
                // It's not the content view but we'll use it as our fallback
                fallback = view
            }
        }

        if (view != null) {
            // Else, we will loop and crawl up the view hierarchy and try to find a parent
            val parent = view.parent
            view = if (parent is View) parent else null
        }
    } while (view != null)

    // If we reach here then we didn't find a CoL or a suitable content view so we'll fallback
    return fallback
}

fun TextView.setTextSource(text: StringSource) {
    this.text = when (text) {
        is StringSource.Text -> text.text
        is StringSource.Res -> context.getString(text.resId)
    }
}

fun TextView.setSpannableOrHiddenText(
    isContentHidden: Boolean, tv: TextView,
    spannable: Spannable?
) {
    if (isContentHidden) {
        val hiddenPattern = "0000"
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        text = hiddenPattern
        tv.visibility = View.VISIBLE
    } else {
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
        text = spannable
        tv.visibility = View.GONE
    }
}

/**
 * Animate progress bar from old to new value
 */
fun ProgressBar.animateProgress(start: Int, stop: Int, duration: Long) {
    ObjectAnimator.ofInt(this, "progress", start, stop).apply {
        this.duration = duration
        start()
    }
}

fun Drawable.startSafe() {
    // Because of strange bug on Meizu Device
    // android.graphics.drawable.AnimatedVectorDrawable cannot be cast to
    // androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
    (this as? android.graphics.drawable.AnimatedVectorDrawable)?.start()
    (this as? androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat)?.start()
}

/**
 * Reduces drag sensitivity of [ViewPager2] widget
 */
fun ViewPager2.reduceDragSensitivity() {
    val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
    recyclerViewField.isAccessible = true
    val recyclerView = recyclerViewField.get(this) as RecyclerView
    val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
    touchSlopField.isAccessible = true
    val touchSlop = touchSlopField.get(recyclerView) as Int
    touchSlopField.set(recyclerView, touchSlop * 8)       // "8" was obtained experimentally
}

fun adjustAlpha(@ColorInt color: Int, factor: Float): Int {
    val alpha = round(Color.alpha(color) * factor).toInt()
    val red = Color.red(color)
    val green = Color.green(color)
    val blue = Color.blue(color)
    return Color.argb(alpha, red, green, blue)
}

fun RecyclerView.removeAllDecorators() {
    for (i in 0 until itemDecorationCount) {
        removeItemDecorationAt(i)
    }
}

fun TextView.setAllCompoundDrawablesTint(@ColorRes color: Int) {
    for (drawable in compoundDrawables) {
        if (drawable != null) {
            drawable.colorFilter = PorterDuffColorFilter(
                context.color(color),
                PorterDuff.Mode.SRC_IN
            )
        }
    }
}

fun TextView.setCompoundDrawableTint(
    @ColorRes colorLeft: Int? = null,
    @ColorRes colorTop: Int? = null,
    @ColorRes colorRight: Int? = null,
    @ColorRes colorBottom: Int? = null,
) {
    compoundDrawables[0]?.let {
        if (colorLeft != null) {
            it.colorFilter = PorterDuffColorFilter(context.color(colorLeft), PorterDuff.Mode.SRC_IN)
        }
    }
    compoundDrawables[1]?.let {
        if (colorTop != null) {
            it.colorFilter = PorterDuffColorFilter(context.color(colorTop), PorterDuff.Mode.SRC_IN)
        }
    }
    compoundDrawables[2]?.let {
        if (colorRight != null) {
            it.colorFilter =
                PorterDuffColorFilter(context.color(colorRight), PorterDuff.Mode.SRC_IN)
        }
    }
    compoundDrawables[3]?.let {
        if (colorBottom != null) {
            it.colorFilter =
                PorterDuffColorFilter(context.color(colorBottom), PorterDuff.Mode.SRC_IN)
        }
    }
}

fun Rect.copy(left: Int? = null, top: Int? = null, right: Int? = null, bottom: Int? = null): Rect {
    return Rect(
        left ?: this.left,
        top ?: this.top,
        right ?: this.right,
        bottom ?: this.bottom
    )
}

/**
 * The method changes the view elevation using the
 * scroll offset [incomingOffset] which could be the scroll offset of app bar,
 * recycler view or nested scroll, etc. The [maxOffset] could be set to restraint
 * the top barrier of elevation changing, basically defines the speed of animation.
 * The [minOffset] could be set to restrain the bottom barrier, where the animation
 * starts.
 *
 * With the scrolling, the elevation of the view will be slowly
 * increased to the maximum. The start elevation should be 0.
 */
fun View.bindElevationToOffset(incomingOffset: Int, maxOffset: Int = 80, minOffset: Int = 0) {
    val maxElevation = 10
    //    context.pxDimen(R.dimen.default_elevation)
    val totalOffsetAvailable = minOffset + maxOffset
    val offset = abs(incomingOffset)
    when {
        offset <= minOffset -> {
            if (elevation > 0f) elevation = 0f
        }

        offset >= totalOffsetAvailable -> {
            if (elevation < maxElevation) elevation = maxElevation.toFloat()
        }

        else -> {
            elevation = offset * maxElevation / totalOffsetAvailable.toFloat()
        }
    }
}

/**
 * The method changes the view alpha using the
 * scroll offset [incomingOffset] which could be the scroll offset of app bar,
 * recycler view or nested scroll, etc. The [maxOffset] could be set to restraint
 * the top barrier of alpha changing, basically defines the speed of animation.
 * The [minOffset] could be set to restrain the bottom barrier, where the animation
 * starts.
 *
 * With the scrolling, the alpha of the view will be slowly
 * increased to the maximum. The start alpha should be 0.
 */
fun View.bindAlphaToOffset(incomingOffset: Int, maxOffset: Int = 80, minOffset: Int = 0) {
    val totalOffsetAvailable = minOffset + maxOffset
    val offset = abs(incomingOffset)
    when {
        offset <= minOffset -> {
            if (alpha > 0f) alpha = 0f
        }

        offset >= totalOffsetAvailable -> {
            if (alpha < 1f) alpha = 1f
        }

        else -> {
            alpha = offset / totalOffsetAvailable.toFloat()
        }
    }
}

/**
 * The method changes the view visibility with fade animation using the
 * scroll offset [incomingOffset] which could be the scroll offset of app bar,
 * recycler view or nested scroll, etc. The [maxOffset] could be set to restraint
 * the top barrier to trigger the animation.
 * The start visibility should be INVISIBLE.
 */
fun View.bindAnimatedVisibilityToOffset(incomingOffset: Int, maxOffset: Int = 80) {
    val offset = abs(incomingOffset)
    val show = when {
        offset >= maxOffset && isInvisible -> true
        offset < maxOffset && isVisible -> false
        else -> null
    }

    // Start animation if we need
    show?.let {
        dimView(
            show = it,
            duration = resources.getInteger(R.integer.short_duration).toLong(),
            useInvisibility = true
        )
    }
}

/**
 * The method returns the parsed color using the
 * scroll offset [incomingOffset]
 *
 * @param incomingOffset - the scroll offset of app bar,
 *                         recycler view or nested scroll
 */
@ColorInt
fun getColorToOffset(
    context: Context,
    incomingOffset: Int,
    maxOffset: Int = 80,
    @ColorRes expandedColor: Int,
    @ColorRes collapsedColor: Int
): Int {
    val offset = abs(incomingOffset)
    val fraction: Float = if (offset >= maxOffset) {
        1f
    } else {
        offset / maxOffset.toFloat()
    }

    return ArgbEvaluatorCompat.getInstance().evaluate(
        fraction,
        context.color(expandedColor),
        context.color(collapsedColor)
    )
}

/**
 * Method allow us to listen clicks on right
 * compound drawable of the text view.
 */
@SuppressLint("ClickableViewAccessibility")
inline fun TextView.setDrawableRightTouch(
    crossinline clickListener: () -> Unit
) {
    val drawableRight = 2
    setOnTouchListener(View.OnTouchListener { _, event ->
        if (event.action == MotionEvent.ACTION_DOWN && event.rawX >= (right - compoundDrawables[drawableRight].bounds.width())) {
            clickListener.invoke()
            return@OnTouchListener true
        }
        false
    }
    )
}

/**
 * Method set text or set visibility invisible if text is null in TextView
 */
fun TextView.setTextOrInvisible(simpleText: String?) {
    when (simpleText) {
        null -> visibility = View.INVISIBLE
        else -> {
            text = simpleText
            visibility = View.VISIBLE
        }
    }
}

/**
 * Method set text or set visibility gone if text is null in TextView
 */
fun TextView.setTextOrGone(simpleText: String?) {
    when (simpleText) {
        null -> visibility = View.GONE
        else -> {
            text = simpleText
            visibility = View.VISIBLE
        }
    }
}

/**
 * Method set text or set visibility invisible if text is null in TextView
 */
fun TextView.setTextOrInvisible(spannableText: SpannableString?) {
    when (spannableText) {
        null -> visibility = View.INVISIBLE
        else -> {
            text = spannableText
            visibility = View.VISIBLE
        }
    }
}

/**
 * Method set text or set visibility gone if text is null in TextView
 */
fun TextView.setTextOrGone(spannableText: SpannableString?) {
    when (spannableText) {
        null -> visibility = View.GONE
        else -> {
            text = spannableText
            visibility = View.VISIBLE
        }
    }
}

/**
 * Method set text or set visibility invisible if text is null in TextView
 */
fun TextView.setTextOrInvisible(spannableText: SpannableStringBuilder?) {
    when (spannableText) {
        null -> visibility = View.INVISIBLE
        else -> {
            text = spannableText
            visibility = View.VISIBLE
        }
    }
}

/**
 * Method set text or set visibility gone if text is null in TextView
 */
fun TextView.setTextOrGone(spannableText: SpannableStringBuilder?) {
    when (spannableText) {
        null -> visibility = View.GONE
        else -> {
            text = spannableText
            visibility = View.VISIBLE
        }
    }
}

/**
 * Method set text from resource in TextView
 */
fun TextView.setTextResource(@StringRes resource: Int) {
    text = context.getString(resource)
}

/**
 * Method set text color from resource in TextView
 */
fun TextView.setTextColorResource(@ColorRes resource: Int) {
    setTextColor(context.color(resource))
}

/**
 * Method set view background color from resource
 */
fun View.setBackgroundColorResource(@ColorRes resource: Int) {
    setBackgroundColor(context.color(resource))
}

fun View.setBackgroundDrawableResource(@DrawableRes resource: Int) {
    background = context.drawable(resource)
}


/**
 * Method set text as html in TextView
 */
fun TextView.setHtmlText(htmlText: String) {
    text = HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY)
}

/**
 * Method set text and set text underline in TextView
 */
fun TextView.setUnderlineText(
    underlineText: String,
    @ColorRes color: Int,
    thickness: Int,
) {
    val spannable = SpannableString(underlineText)
    spannable.setSpan(object : UnderlineSpan() {
        override fun updateDrawState(ds: TextPaint) {
            try {
                val method: Method = TextPaint::class.java.getMethod(
                    "setUnderlineText",
                    Integer.TYPE,
                    java.lang.Float.TYPE
                )
                method.invoke(
                    ds,
                    context.color(color),
                    thickness
                )
            } catch (e: Exception) {
                ds.isUnderlineText = true
            }
        }
    }, 0, spannable.length, 0)
    text = spannable
}

inline fun AppCompatEditText.setTextChangeListener(crossinline text: (String) -> Unit) {
    addTextChangedListener { editable ->
        text(editable?.toString() ?: "")
    }
}

fun <T : ViewBinding> ViewGroup.inflateBinding(
    inflateFunc: (LayoutInflater, ViewGroup, Boolean) -> T
): T = inflateFunc(LayoutInflater.from(context), this, false)

fun RecyclerView.enableChangeAnimations(isEnable: Boolean) {
    (this.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = isEnable
}

fun TextView.clearCompoundDrawables() {
    setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
}

fun TextView.setCompoundDrawablesExt(
    start: Drawable? = null,
    top: Drawable? = null,
    end: Drawable? = null,
    bottom: Drawable? = null
) {
    setCompoundDrawablesWithIntrinsicBounds(start, top, end, bottom)
}

fun TextView.setCompoundDrawablesExt(
    @DrawableRes start: Int = 0,
    @DrawableRes top: Int = 0,
    @DrawableRes end: Int = 0,
    @DrawableRes bottom: Int = 0,
) {
    setCompoundDrawablesWithIntrinsicBounds(start, top, end, bottom)
}

fun LottieAnimationView.addOverlayColor(@ColorRes colorRes: Int?) {
    addValueCallback(KeyPath("**"), COLOR_FILTER) {
        colorRes?.let(context::color)?.let(::SimpleColorFilter)
    }
}

fun RadioGroup.applyColorStateList(colorStateList: ColorStateList) {
    allViews
        .map { it as? RadioButton }
        .forEach { it?.buttonTintList = colorStateList }
}

fun RecyclerView.getFirstVisiblePosition(): Int? {
    return (layoutManager as? LinearLayoutManager?)?.findFirstCompletelyVisibleItemPosition()
}