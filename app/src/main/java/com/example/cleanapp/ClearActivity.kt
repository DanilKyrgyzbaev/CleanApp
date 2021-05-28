package com.example.cleanapp

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.example.cleanapp.databinding.ActivityClearBinding
import java.text.DecimalFormat


class ClearActivity : AppCompatActivity() {

    lateinit var binding: ActivityClearBinding

    var _isTotalSizeAnimated: MutableLiveData<Boolean> = MutableLiveData()

    var isClearingReady = false
    var isCleaned = false

    var currentValue = 0.0
    var currentType = ""
    var countdownCurrentType = ""
    var totalRandomSizeValue = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClearBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkIsCleaned()
    }

    private fun checkIsCleaned() {
//        val isCleaned: Boolean = (PreferenceHelper.getInstance(this)?.isCleaned) as Boolean
//        PreferenceHelper.getInstance(this)?.isCleaned = true
//        Toast.makeText(this, "isCleaned: $isCleaned", Toast.LENGTH_SHORT).show()
        if (!isCleaned){
            setAnimation()
            setBackButton()
            setDetailSizeData()
        } else {
            Toast.makeText(this, "Дождитесь 10 секунд", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setBackButton() {
        binding.backImg.setOnClickListener {
            finish()
        }
    }

    private fun startCountAnimation(until: Long) {
        _isTotalSizeAnimated.value = false
        isClearingReady = false
        isCleaned = false
//        val randomSize = (0..until).random()
        val animator = ValueAnimator.ofInt(0, until.toInt())
        animator.duration = 5000
        animator.addUpdateListener { animation ->
            if (animation.animatedValue.toString().toInt() > 1000) {
                binding.txtClearSize.text =
                    prettyCount(animation.animatedValue.toString().toDouble())
            } else {
                binding.txtClearSize.text = animation.animatedValue.toString()
            }
            if (binding.txtClearSize.text.toString().toDouble() < 99) {
                binding.txtClearType.text = "б"
                currentType = "б"
            }
            if (binding.txtClearSize.text.toString().toDouble() > 100) {
                binding.txtClearType.text = "мб"
                currentType = "мб"
            }
            if (binding.txtClearSize.text.toString().contains(".")) {
                binding.txtClearType.text = "гб"
                currentType = "гб"
            }
            currentValue = animation.animatedValue.toString().toDouble()
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                stopAnimForRefreshIcons()
                replaceRefreshIconsForGrey()
                Handler().postDelayed({
                    stopAnimForMainEllipse()
                }, 500)
                setDetailSizeTxtVisible()

                if (currentValue > 1000) {
                    setButtonClearSize(
                        "Очистить ${
                            prettyCount(
                                currentValue.toString().toDouble()
                            )
                        } ${currentType.toUpperCase()}"
                    )
                } else {
                    setButtonClearSize("Очистить $currentValue ${currentType.toUpperCase()}")
                }
                isClearingReady = true
                _isTotalSizeAnimated.value = true
                setupButtonListener()
            }


            override fun onAnimationCancel(animation: Animator?) {}

            override fun onAnimationRepeat(animation: Animator?) {}
        })
        animator.start()
    }

    fun startCountdown() {
        val animator = ValueAnimator.ofInt(currentValue.toInt(), 0)
        animator.duration = 5000
        animator.addUpdateListener { animation ->
            if (animation.animatedValue.toString().toInt() > 1000) {
                binding.txtClearSize.text =
                    prettyCount(animation.animatedValue.toString().toDouble())
            } else {
                binding.txtClearSize.text = animation.animatedValue.toString()
            }
            if (binding.txtClearSize.text.toString().toDouble() < 99) {
                binding.txtClearType.text = "б"
                currentType = "б"
            }
            if (binding.txtClearSize.text.toString().toDouble() > 100) {
                binding.txtClearType.text = "мб"
                currentType = "мб"
            }
            if (binding.txtClearSize.text.toString().contains(".")) {
                binding.txtClearType.text = "гб"
                currentType = "гб"
            }
            currentValue = animation.animatedValue.toString().toDouble()
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
                Handler().postDelayed({
                    replaceRefreshIconsForDone()
                }, 2500)
            }

            override fun onAnimationEnd(animation: Animator?) {
                isCleaned = true
                stopAnimForMainEllipse()
                setDetailSizeDataToZero()
                binding.btnMain.text = "Готово"
                binding.btnMain.setOnClickListener {
                    finish()
                }

//                startAlarmManager()
            }


            override fun onAnimationCancel(animation: Animator?) {}

            override fun onAnimationRepeat(animation: Animator?) {}
        })
        animator.start()
    }

//    fun SetAlarm() {
////        val button: Button = buttons.get(2) // replace with a button from your own UI
//        val receiver: BroadcastReceiver = object : BroadcastReceiver() {
//            override fun onReceive(context: Context, intent: Intent) {
////                button.setBackgroundColor(Color.RED)
//                Log.d("OLOLO", "AWDAWDAWDAWDADW")
//                context.unregisterReceiver(this) // this == BroadcastReceiver, not Activity
//            }
//        }
//        this.registerReceiver(receiver, IntentFilter("com.blah.blah.somemessage"))
//        val pintent = PendingIntent.getBroadcast(this, 0, Intent("com.blah.blah.somemessage"), 0)
//        val manager = this.getSystemService(ALARM_SERVICE) as AlarmManager
//
//        // set alarm to fire 5 sec (1000*5) from now (SystemClock.elapsedRealtime())
//        manager[AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000 * 5] =
//            pintent
//    }

//    private fun startAlarmManager() {
//        if (!PreferenceHelper.getInstance(this)?.isCleaned!!){
//            PreferenceHelper.getInstance(this)?.isCleaned = true
//            val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//            val intent = Intent(this, Receiver::class.java)
//
//            // Used for filtering inside Broadcast receiver
//            intent.action = "MyBroadcastReceiverAction"
//            val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
//
//            // In this particular example we are going to set it to trigger after 30 seconds.
//            // You can work with time later when you know this works for sure.
//            val msUntilTriggerHour: Long = 25000
//            val alarmTimeAtUTC: Long = System.currentTimeMillis() + msUntilTriggerHour
//
//            // Depending on the version of Android use different function for setting an
//            // Alarm.
//            // setAlarmClock() - used for everything lower than Android M
//            // setExactAndAllowWhileIdle() - used for everything on Android M and higher
//            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
//                alarmManager.setAlarmClock(
//                    AlarmManager.AlarmClockInfo(alarmTimeAtUTC, pendingIntent),
//                    pendingIntent
//                )
//            } else {
//                alarmManager.setExactAndAllowWhileIdle(
//                    AlarmManager.RTC_WAKEUP,
//                    alarmTimeAtUTC,
//                    pendingIntent
//                )
//            }
//        }
//    }

    private fun setDetailSizeDataToZero() {
        binding.txtDetailSize1.text = "0 Б"
        binding.txtDetailSize2.text = "0 Б"
        binding.txtDetailSize3.text = "0 Б"
        binding.txtDetailSize4.text = "0 Б"
        binding.txtDetailSize5.text = "0 Б"
    }

    private fun setDetailSizeData() {
        val random1 = (0..1000L).random()
        val random2 = (0..4000L).random()
        val random3 = (0..4000L).random()
        val random4 = (0..2000L).random()
        val random5 = (0..3000L).random()

        totalRandomSizeValue = (random1 + random2 + random3 + random4 + random5)

        Toast.makeText(this, "total: $totalRandomSizeValue", Toast.LENGTH_SHORT).show()
        startCountAnimation(totalRandomSizeValue)

        _isTotalSizeAnimated.observe(this) {
            if (it) {
                if (random1 >= 1000) {
                    countdownCurrentType = "гб"
                    binding.txtDetailSize1.text =
                        "${prettyCount(random1)} ${countdownCurrentType.toUpperCase()}"
                } else if (random1 < 99) {
                    countdownCurrentType = "б"
                    binding.txtDetailSize1.text = "${random1} ${countdownCurrentType.toUpperCase()}"
                } else if (random1 > 100) {
                    countdownCurrentType = "мб"
                    binding.txtDetailSize1.text = "${random1} ${countdownCurrentType.toUpperCase()}"
                }

                if (random2 >= 1000) {
                    countdownCurrentType = "гб"
                    binding.txtDetailSize2.text =
                        "${prettyCount(random2)} ${countdownCurrentType.toUpperCase()}"
                } else if (random2 < 99) {
                    countdownCurrentType = "б"
                    binding.txtDetailSize2.text = "${random2} ${countdownCurrentType.toUpperCase()}"
                } else if (random2 > 100) {
                    countdownCurrentType = "мб"
                    binding.txtDetailSize2.text = "${random2} ${countdownCurrentType.toUpperCase()}"
                }

                if (random3 >= 1000) {
                    countdownCurrentType = "гб"
                    binding.txtDetailSize3.text =
                        "${prettyCount(random3)} ${countdownCurrentType.toUpperCase()}"
                } else if (random3 < 99) {
                    countdownCurrentType = "б"
                    binding.txtDetailSize3.text = "${random3} ${countdownCurrentType.toUpperCase()}"
                } else if (random3 > 100) {
                    countdownCurrentType = "мб"
                    binding.txtDetailSize3.text = "${random3} ${countdownCurrentType.toUpperCase()}"
                }

                if (random4 >= 1000) {
                    countdownCurrentType = "гб"
                    binding.txtDetailSize4.text =
                        "${prettyCount(random4)} ${countdownCurrentType.toUpperCase()}"
                } else if (random4 < 99) {
                    countdownCurrentType = "б"
                    binding.txtDetailSize4.text = "${random4} ${countdownCurrentType.toUpperCase()}"
                } else if (random4 > 100) {
                    countdownCurrentType = "мб"
                    binding.txtDetailSize4.text = "${random4} ${countdownCurrentType.toUpperCase()}"
                }

                if (random5 >= 1000) {
                    countdownCurrentType = "гб"
                    binding.txtDetailSize5.text =
                        "${prettyCount(random5)} ${countdownCurrentType.toUpperCase()}"
                } else if (random5 < 99) {
                    countdownCurrentType = "б"
                    binding.txtDetailSize5.text = "${random5} ${countdownCurrentType.toUpperCase()}"
                } else if (random4 > 100) {
                    countdownCurrentType = "мб"
                    binding.txtDetailSize5.text = "${random5} ${countdownCurrentType.toUpperCase()}"
                }
            }
        }
    }

    fun setupButtonListener() {
        binding.btnMain.setOnClickListener {
            if (isClearingReady) {
                startCountdown()
                startAnimForMainEllipse()
            }
        }
    }

    fun setDetailSizeTxtVisible() {
        binding.txtDetailSize1.visibility = View.VISIBLE
        binding.txtDetailSize2.visibility = View.VISIBLE
        binding.txtDetailSize3.visibility = View.VISIBLE
        binding.txtDetailSize4.visibility = View.VISIBLE
        binding.txtDetailSize5.visibility = View.VISIBLE
    }

    fun setDetailSizeTxtGone() {
        binding.txtDetailSize1.visibility = View.GONE
        binding.txtDetailSize2.visibility = View.GONE
        binding.txtDetailSize3.visibility = View.GONE
        binding.txtDetailSize4.visibility = View.GONE
        binding.txtDetailSize5.visibility = View.GONE
    }

    fun stopAnimForMainEllipse() {
        binding.ellipse1.clearAnimation()
    }

    fun setButtonClearSize(value: String) {
        binding.btnMain.setBackgroundDrawable(resources.getDrawable(R.drawable.bg_button))
        binding.btnMain.text = value
        binding.btnMain.setTextColor(resources.getColor(R.color.white))
    }

    fun startAnimForMainEllipse() {
        val rotation: Animation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate)
        rotation.repeatCount = Animation.INFINITE
        binding.ellipse1.startAnimation(rotation)
    }

    fun replaceRefreshIconsForGrey() {
        val random1 = (0..1500L).random()
        val random2 = (0..1500L).random()
        val random3 = (0..1500L).random()
        val random4 = (0..1500L).random()
        val random5 = (0..1500L).random()
        Handler().postDelayed({
            binding.imgUpdating1.setImageResource(R.drawable.ic_grey)
            binding.text1.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                resources.getDrawable(R.drawable.razvernut),
                null
            )
        }, random1)
        Handler().postDelayed({
            binding.imgUpdating2.setImageResource(R.drawable.ic_grey)
            binding.text2.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                resources.getDrawable(R.drawable.razvernut),
                null
            )
        }, random2)
        Handler().postDelayed({
            binding.imgUpdating3.setImageResource(R.drawable.ic_grey)
            binding.text3.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                resources.getDrawable(R.drawable.razvernut),
                null
            )
        }, random3)
        Handler().postDelayed({
            binding.imgUpdating4.setImageResource(R.drawable.ic_grey)
            binding.text4.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                resources.getDrawable(R.drawable.razvernut),
                null
            )
        }, random4)
        Handler().postDelayed({
            binding.imgUpdating5.setImageResource(R.drawable.ic_grey)
            binding.text5.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                resources.getDrawable(R.drawable.razvernut),
                null
            )
        }, random5)
    }


    fun replaceRefreshIconsForDone() {
        val random1 = (0..1500L).random()
        val random2 = (0..1500L).random()
        val random3 = (0..1500L).random()
        val random4 = (0..1500L).random()
        val random5 = (0..1500L).random()
        Handler().postDelayed({
            binding.imgUpdating1.setImageResource(R.drawable.ic_done)
        }, random1)
        Handler().postDelayed({
            binding.imgUpdating2.setImageResource(R.drawable.ic_done)
        }, random2)
        Handler().postDelayed({
            binding.imgUpdating3.setImageResource(R.drawable.ic_done)
        }, random3)
        Handler().postDelayed({
            binding.imgUpdating4.setImageResource(R.drawable.ic_done)
        }, random4)
        Handler().postDelayed({
            binding.imgUpdating5.setImageResource(R.drawable.ic_done)
        }, random5)
    }

    fun stopAnimForRefreshIcons() {
        binding.imgUpdating1.clearAnimation()
        binding.imgUpdating2.clearAnimation()
        binding.imgUpdating3.clearAnimation()
        binding.imgUpdating4.clearAnimation()
        binding.imgUpdating5.clearAnimation()
    }

    fun prettyCount(number: Number): String? {
        val numValue = number.toLong()
        val value = Math.floor(Math.log10(numValue.toDouble())).toInt()
        val base = value / 3
        return if (value >= 3) {
            DecimalFormat("#0.0").format(
                numValue / Math.pow(
                    10.0,
                    (base * 3).toDouble()
                )
            )
        } else {
            DecimalFormat("#,##0").format(numValue)
        }
    }

    private fun setAnimation() {
        val rotation: Animation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate)
        rotation.repeatCount = Animation.INFINITE
        binding.ellipse1.startAnimation(rotation)


        val rotate = RotateAnimation(
            0f, 360f,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
        )
        rotate.duration = 1000
        rotate.repeatCount = Animation.INFINITE
        rotate.repeatMode = Animation.INFINITE
        rotate.interpolator = LinearInterpolator()

        binding.imgUpdating1.startAnimation(rotate)
        binding.imgUpdating2.startAnimation(rotate)
        binding.imgUpdating3.startAnimation(rotate)
        binding.imgUpdating4.startAnimation(rotate)
        binding.imgUpdating5.startAnimation(rotate)
    }
}