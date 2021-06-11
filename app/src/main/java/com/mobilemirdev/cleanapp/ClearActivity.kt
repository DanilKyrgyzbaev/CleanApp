package com.example.cleanapp

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.ContentValues.TAG
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
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
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.text.DecimalFormat

const val AD_UNIT_ID = "ca-app-pub-8662482372360135~5394086193"


class ClearActivity : AppCompatActivity() {

    lateinit var binding: ActivityClearBinding

    var _isTotalSizeAnimated: MutableLiveData<Boolean> = MutableLiveData()

    var isClearingReady = false
    var isCleaned = false

    var currentValue = 0.0
    var currentType = ""
    var countdownCurrentType = ""
    var totalRandomSizeValue = 0L

    private var mInterstitialAd: InterstitialAd? = null
    private var mCountDownTimer: CountDownTimer? = null
    private var mGameIsInProgress = false
    private var mAdIsLoading: Boolean = false
    private var mTimerMilliseconds = 0L
    private var TAG = "ClearActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClearBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this) {}

        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(listOf("ABCDEF012345"))
                .build()
        )

        startAdMod()
        setAnimation()
        setBackButton()
        setDetailSizeData()

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
        val animator = ValueAnimator.ofInt(0, until.toInt())
        animator.duration = 5000
        animator.addUpdateListener { animation ->
            if (animation.animatedValue.toString().toInt() > 1000.0) {
                binding.txtClearSize.text =
                    prettyCount(animation.animatedValue.toString().toDouble())
            } else {
                binding.txtClearSize.text = animation.animatedValue.toString()
            }
            if (binding.txtClearSize.text.toString().replace(",", ".").toDouble() < 99.0) {
                binding.txtClearType.text = "б"
                currentType = "б"
            }
            if (binding.txtClearSize.text.toString().replace(",", ".").toDouble() > 100.0) {
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
                binding.btnMain.isEnabled = false
                binding.btnMain.text = "Подождите..."
            }

            override fun onAnimationEnd(animation: Animator?) {
                stopAnimForRefreshIcons()
                replaceRefreshIconsForGrey()
                Handler().postDelayed({
                    stopAnimForMainEllipse()
                }, 500)
                setDetailSizeTxtVisible()

                if (currentValue > 1000) {
                    binding.btnMain.isEnabled = true
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

    private fun startCountdown() {
        val animator = ValueAnimator.ofInt(currentValue.toInt(), 0)
        animator.duration = 5000
        animator.addUpdateListener { animation ->
            if (animation.animatedValue.toString().toInt() > 1000) {
                binding.txtClearSize.text =
                    prettyCount(animation.animatedValue.toString().toDouble())
            } else {
                binding.txtClearSize.text = animation.animatedValue.toString()
            }
            if (binding.txtClearSize.text.toString().replace(",", ".").toDouble() < 99) {
                binding.txtClearType.text = "б"
                currentType = "б"
            }
            if (binding.txtClearSize.text.toString().replace(",", ".").toDouble() > 100) {
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
                binding.btnMain.text = "Подождите..."
                binding.btnMain.isEnabled = false
                Handler().postDelayed({
                    replaceRefreshIconsForDone()
                }, 2500)
            }

            override fun onAnimationEnd(animation: Animator?) {
                isCleaned = true
                binding.btnMain.isEnabled = true
                binding.ellipse1.setImageResource(R.drawable.ic_ellipsize_green)
                binding.btnMain.background = resources.getDrawable(R.drawable.bg_button_green)
                stopAnimForMainEllipse()
                setDetailSizeDataToZero()
                replaceRefreshIconsForDoneGreen()
                binding.btnMain.text = "Готово"
                binding.btnMain.setOnClickListener {
                    finish()
                }
            }


            override fun onAnimationCancel(animation: Animator?) {}

            override fun onAnimationRepeat(animation: Animator?) {}
        })
        animator.start()
    }

    private fun setDetailSizeDataToZero() {
        binding.txtDetailSize1.text = "0 Б"
        binding.txtDetailSize2.text = "0 Б"
        binding.txtDetailSize3.text = "0 Б"
        binding.txtDetailSize4.text = "0 Б"
        binding.txtDetailSize5.text = "0 Б"
    }

    private fun setDetailSizeData() {
        val random1 = (0..1000L).random()
        val random2 = (0..1000L).random()
        val random3 = (0..1000L).random()
        val random4 = (0..1000L).random()
        val random5 = (0..1000L).random()

        totalRandomSizeValue = (random1 + random2 + random3 + random4 + random5)

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
                showInterstitial()

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
        val random1 = (0..250L).random()
        val random2 = (0..250L).random()
        val random3 = (0..250L).random()
        val random4 = (0..250L).random()
        val random5 = (0..250L).random()
        Handler().postDelayed({
            binding.imgUpdating1.setImageResource(R.drawable.ic_grey)
  
        }, random1)
        Handler().postDelayed({
            binding.imgUpdating2.setImageResource(R.drawable.ic_grey)

        }, random2)
        Handler().postDelayed({
            binding.imgUpdating3.setImageResource(R.drawable.ic_grey)

        }, random3)
        Handler().postDelayed({
            binding.imgUpdating4.setImageResource(R.drawable.ic_grey)

        }, random4)
        Handler().postDelayed({
            binding.imgUpdating5.setImageResource(R.drawable.ic_grey)


        }, random5)
    }


    fun replaceRefreshIconsForDone() {
        val random1 = (0..250L).random()
        val random2 = (0..250L).random()
        val random3 = (0..250L).random()
        val random4 = (0..250L).random()
        val random5 = (0..250L).random()
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

    fun replaceRefreshIconsForDoneGreen() {
        val random1 = (0..250L).random()
        val random2 = (0..250L).random()
        val random3 = (0..250L).random()
        val random4 = (0..250L).random()
        val random5 = (0..250L).random()
        Handler().postDelayed({
            binding.imgUpdating1.setImageResource(R.drawable.ic_done_green)
        }, random1)
        Handler().postDelayed({
            binding.imgUpdating2.setImageResource(R.drawable.ic_done_green)
        }, random2)
        Handler().postDelayed({
            binding.imgUpdating3.setImageResource(R.drawable.ic_done_green)
        }, random3)
        Handler().postDelayed({
            binding.imgUpdating4.setImageResource(R.drawable.ic_done_green)
        }, random4)
        Handler().postDelayed({
            binding.imgUpdating5.setImageResource(R.drawable.ic_done_green)
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

    private fun loadAd() {
        var adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            this, AD_UNIT_ID, adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError?.message)
                    mInterstitialAd = null
                    mAdIsLoading = false
                    val error = "domain: ${adError.domain}, code: ${adError.code}, " +
                            "message: ${adError.message}"
                    Toast.makeText(
                        this@ClearActivity,
                        "onAdFailedToLoad() with error $error",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(TAG, "Ad was loaded.")
                    mInterstitialAd = interstitialAd
                    mAdIsLoading = false
                    Toast.makeText(this@ClearActivity, "onAdLoaded()", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun showInterstitial() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Ad was dismissed.")
                    // Don't forget to set the ad reference to null so you
                    // don't show the ad a second time.
                    mInterstitialAd = null
                    loadAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                    Log.d(TAG, "Ad failed to show.")
                    // Don't forget to set the ad reference to null so you
                    // don't show the ad a second time.
                    mInterstitialAd = null
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Ad showed fullscreen content.")
                    // Called when ad is dismissed.
                }
            }
            mInterstitialAd?.show(this)
        } else {
            Toast.makeText(this, "Ad wasn't loaded.", Toast.LENGTH_SHORT).show()
            startAdMod()

        }
    }

    private fun startAdMod() {
        if (!mAdIsLoading && mInterstitialAd == null) {
            mAdIsLoading = true
            loadAd()
        }
    }

    private fun resumeAdMod(milliseconds: Long) {
        // Create a new timer for the correct length and start it.
        mGameIsInProgress = true
        mTimerMilliseconds = milliseconds
        mCountDownTimer?.start()
    }

    // Resume the AdMod if it's in progress.
    public override fun onResume() {
        super.onResume()

        if (mGameIsInProgress) {
            resumeAdMod(mTimerMilliseconds)
        }
    }

    // Cancel the timer if the AdMod is paused.
    public override fun onPause() {
        mCountDownTimer?.cancel()
        super.onPause()
    }
}