package com.hyphenate.easeui.feature.thread

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.R
import com.hyphenate.easeui.base.EaseBaseActivity
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.easeui.common.EaseConstant
import com.hyphenate.easeui.common.extensions.hasRoute
import com.hyphenate.easeui.common.permission.PermissionCompat
import com.hyphenate.easeui.common.permission.PermissionsManager
import com.hyphenate.easeui.databinding.EaseActivityChatThreadBinding
import com.hyphenate.easeui.feature.chat.EaseChatFragment
import com.hyphenate.easeui.feature.chat.interfaces.OnChatExtendMenuItemClickListener
import com.hyphenate.easeui.feature.chat.interfaces.OnChatRecordTouchListener
import com.hyphenate.easeui.feature.thread.fragment.EaseChatThreadFragment
import com.hyphenate.easeui.feature.thread.interfaces.OnJoinChatThreadResultListener

open class EaseChatThreadActivity: EaseBaseActivity<EaseActivityChatThreadBinding>() {

    private val requestImagePermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            onRequestResult(
                result,
                REQUEST_CODE_STORAGE_PICTURE
            )
        }
    private val requestVideoPermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            onRequestResult(
                result,
                REQUEST_CODE_STORAGE_VIDEO
            )
        }
    private val requestFilePermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            onRequestResult(
                result,
                REQUEST_CODE_STORAGE_FILE
            )
        }

    private var conversationId: String? = ""
    private var topicMsgId: String? = ""
    private var threadId: String? = ""
    private var msgId:String? = ""
    private var fragment: EaseChatFragment? = null

    override fun getViewBinding(inflater: LayoutInflater): EaseActivityChatThreadBinding {
        return EaseActivityChatThreadBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        conversationId = intent.getStringExtra(EaseConstant.EXTRA_CONVERSATION_ID)
        topicMsgId = intent.getStringExtra(EaseConstant.THREAD_TOPIC_MESSAGE_ID)
        msgId = intent.getStringExtra(EaseConstant.THREAD_MESSAGE_ID)
        threadId = intent.getStringExtra(EaseConstant.THREAD_CHAT_THREAD_ID)

        initData()
    }

    open fun initData(){
        val builder = EaseChatThreadFragment.Builder(conversationId,threadId,topicMsgId,msgId)
            .setOnJoinThreadResultListener(object : OnJoinChatThreadResultListener{
                override fun joinSuccess(threadId: String?) {
                    ChatLog.e(TAG,"joinSuccess $threadId")
                }

                override fun joinFailed(code: Int, error: String?) {
                    ChatLog.e(TAG,"joinFailed $code $error")
                }
            })
            .setEmptyLayout(R.layout.ease_layout_no_data_show_nothing)
            .setOnChatExtendMenuItemClickListener(object : OnChatExtendMenuItemClickListener {
                override fun onChatExtendMenuItemClick(view: View?, itemId: Int): Boolean {
                    when(itemId) {
                        R.id.extend_item_take_picture -> {
                            if (!PermissionsManager.getInstance()
                                    .hasPermission(mContext, Manifest.permission.CAMERA)
                            ) {
                                PermissionsManager.getInstance()
                                    .requestPermissionsIfNecessaryForResult(
                                        mContext,
                                        arrayOf(Manifest.permission.CAMERA),
                                        null
                                    )
                                return true
                            }
                        }
                        R.id.extend_item_picture -> {
                            if (!PermissionCompat.checkMediaPermission(
                                    mContext,
                                    requestImagePermission,
                                    Manifest.permission.READ_MEDIA_IMAGES
                                )
                            ) {
                                return true
                            }
                        }

                        R.id.extend_item_video -> {
                            if (!PermissionCompat.checkMediaPermission(
                                    mContext,
                                    requestVideoPermission,
                                    Manifest.permission.READ_MEDIA_VIDEO,
                                    Manifest.permission.CAMERA
                                )
                            ) {
                                return true
                            }
                        }

                        R.id.extend_item_file -> {
                            if (!PermissionCompat.checkMediaPermission(
                                    mContext,
                                    requestFilePermission,
                                    Manifest.permission.READ_MEDIA_IMAGES,
                                    Manifest.permission.READ_MEDIA_VIDEO
                                )
                            ) {
                                return true
                            }
                        }
                    }
                    return false
                }
            })
            .setOnChatRecordTouchListener(object : OnChatRecordTouchListener {
                override fun onRecordTouch(v: View?, event: MotionEvent?): Boolean {
                    if (!PermissionsManager.getInstance()
                            .hasPermission(mContext, Manifest.permission.RECORD_AUDIO)
                    ) {
                        PermissionsManager.getInstance()
                            .requestPermissionsIfNecessaryForResult(
                                mContext,
                                arrayOf(Manifest.permission.RECORD_AUDIO),
                                null
                            )
                        return true
                    }
                    return false
                }
            })
            .setTitleBarBackPressListener{
                finish()
            }
            .useTitleBar(true)
            .enableTitleBarPressBack(true)

        setChildSettings(builder)
        fragment = builder.build()
        fragment?.let { fragment ->
            supportFragmentManager.beginTransaction().replace(binding.flFragment.id, fragment).commit()
        }
    }

    protected open fun setChildSettings(builder: EaseChatFragment.Builder) {}

    private fun onRequestResult(result: Map<String, Boolean>?, requestCode: Int) {
        if (!result.isNullOrEmpty()) {
            for ((key, value) in result) {
                ChatLog.e(TAG, "onRequestResult: $key  $value")
            }
            if (PermissionCompat.getMediaAccess(mContext) !== PermissionCompat.StorageAccess.Denied) {
                if (requestCode == REQUEST_CODE_STORAGE_PICTURE) {
                    fragment?.selectPicFromLocal()
                } else if (requestCode == REQUEST_CODE_STORAGE_VIDEO) {
                    fragment?.selectVideoFromLocal()
                } else if (requestCode == REQUEST_CODE_STORAGE_FILE) {
                    fragment?.selectFileFromLocal()
                }
            }
        }
    }

    companion object {
        private val TAG = EaseChatThreadActivity::class.java.simpleName
        private const val REQUEST_CODE_STORAGE_PICTURE = 111
        private const val REQUEST_CODE_STORAGE_VIDEO = 112
        private const val REQUEST_CODE_STORAGE_FILE = 113
        fun actionStart(context: Context, conversationId:String, threadId:String, topicMsgId:String, msgId:String? = null) {
            val intent = Intent(context, EaseChatThreadActivity::class.java)
            intent.putExtra(EaseConstant.EXTRA_CONVERSATION_ID,conversationId)
            intent.putExtra(EaseConstant.THREAD_CHAT_THREAD_ID,threadId)
            intent.putExtra(EaseConstant.THREAD_TOPIC_MESSAGE_ID,topicMsgId)
            msgId?.let {
                intent.putExtra(EaseConstant.THREAD_MESSAGE_ID,msgId)
            }
            EaseIM.getCustomActivityRoute()?.getActivityRoute(intent.clone() as Intent)?.let {
                if (it.hasRoute()) {
                    context.startActivity(it)
                    return
                }
            }
            context.startActivity(intent)
        }
    }

}