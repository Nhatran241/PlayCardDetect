package com.machinelearning.playcarddetect.modules.client.service

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Rect
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.machinelearning.playcarddetect.modules.accessibilityaction.action.OpenApp
import com.machinelearning.playcarddetect.common.model.CardBase64
import com.machinelearning.playcarddetect.common.setNotification
import com.machinelearning.playcarddetect.modules.accessibilityaction.action.GestureAction
import com.machinelearning.playcarddetect.modules.datamanager.CaptureManager
import com.machinelearning.playcarddetect.modules.datamanager.ServerClientDataManager
import com.machinelearning.playcarddetect.modules.datamanager.TextCollectionManager.CurrentPosition
import com.nhatran241.accessibilityactionmodule.BaseActionService
import com.nhatran241.accessibilityactionmodule.model.ClickAction
import com.nhatran241.accessibilityactionmodule.model.SwipeAction
import java.util.*
import kotlin.math.log


class ClientResponseDataService : BaseActionService(){
    companion object{
        var isConnected = false
        const val TAG ="acessibilityService"
    }
    private var mHandler: Handler? = null
    private var captureManager: CaptureManager? = null


    /**
     * Data
     */
    private val listCardInHand: MutableList<CardBase64> = ArrayList()
    private var scaleRatio = 0f
    private var newHeight = 0
    private var newWidth = 0
    private var screenWidth = 0
    private var screenHeight = 0
    private var currentPosition = CurrentPosition.Undetected
    private var numberRoomRect = Rect(113, 36, 147, 71)

    /**
     * Click
     */
    override fun onServiceConnected() {
        super.onServiceConnected()
        setNotification()
        isConnected = true
        captureManager = CaptureManager.getInstance()
        startCapture()
        val display = (getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay
        val size = Point()
        display.getRealSize(size)
        //            if(width<height) {
        if (size.x < size.y) {
            screenWidth = size.y
            screenHeight = size.x
        } else {
            screenWidth = size.x
            screenHeight = size.y
        }


        /**
         * Register Self device to server too handle remote event
         */
//        ServerClientDataManager.getInstance().ClientPushRoom("0",object:ServerClientDataManager.IClientCallbackToRoomPath{
//            override fun onSuccess() {
//                Log.d(TAG, "onServiceConnected")
//            }
//
//            override fun onFailed(error: String?) {
//
//                Log.d(TAG, "onServiceConnected: $error")
//            }
//
//        })
        ServerClientDataManager.getInstance().ClientListenerToRemotePath {
            if (it is OpenApp){
                val launchIntent = packageManager.getLaunchIntentForPackage(it.packagename)
                launchIntent?.let { startActivity(it) }
            }else if (it is GestureAction){
                Log.d(TAG, "onServiceConnected: $it")
                performAction(mutableListOf(it)) { it ->
                    Toast.makeText(this,it,Toast.LENGTH_SHORT).show()
                }
            }
        }

//        ServerClientDataManager.getInstance().RegisterClientToRemoteServer(this,object:ServerClientDataManager.IRegisterClientToRemoteServer{
//            override fun onRegisterClientToRemoteServerFailed(errro: String?) {
//                Toast.makeText(this@ClientResponseDataService,""+errro,Toast.LENGTH_SHORT).show()
//            }
//
//            override fun onRegisterClientToRemoteServerSuccess() {
//                Log.d("nhatnhat","Successs")
//            }
//
//            override fun onReponseFromRemoteServer(action: String?) {
//                Log.d("nhatnhat", "Action $action")
//            }
//
//        })
//


//        getInstance().prepareClientServer(this, false, object : IClientPrepareListener {
//            override fun OnPrepareClientServerSuccess() {
//                getInstance().RegisterClientListenerWithServer(object : IClientListener {
//                    override fun OnServerClickCard(position: Int) {
//                        currentPosition = CurrentPosition.PLaying
//                        val cardRect = listCardInHand[position].cardRect
//                        var y = newHeight - newHeight / 3 + cardRect.top + cardRect.centerY()
//                        var x = cardRect.centerX() //512/1024
//                        // ? /1570
//                        (x *= screenWidth.toDouble() / newWidth).toInt()
//                        (y *= screenHeight.toDouble() / newHeight).toInt()
//                        click(x, y)
//                    }
//
//                    override fun OnServerClickXepBai() {}
//                })
//            }
//
//            override fun OnPrepareClientServerFail(error: String) {
//                Toast.makeText(this@ClientService, "" + error, Toast.LENGTH_SHORT).show()
//            }
//        })
    }

    private fun startCapture() {
        captureManager?.setListener {
            handleBitmap(it)
        }
    }

    private fun handleBitmap(it: Bitmap?) {
//        if (it != null) {
////            if (currentPosition == CurrentPosition.PLaying) {
////                prepareAndPutData(bitmap)
////            } else {
//                checkRoomNumber(it)
////            }
//        } else {
            captureManager!!.takeScreenshot()
//        }

    }



//    override fun onCreate() {
//        super.onCreate()
//        Log.d("nhatnhat", "onCreated: ")
//        val handlerThread = HandlerThread("auto-handler")
//        handlerThread.start()
//        mHandler = Handler(handlerThread.looper)
//    }


    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.d(TAG, "onAccessibilityEvent: " + event.toString())
    }

    override fun onInterrupt() {
        Log.d(TAG, "onInterrupt: ")
    }

//    private fun click(x: Int, y: Int) {
//        val builder = GestureDescription.Builder()
//        val path = Path()
//        path.moveTo(x.toFloat(), y.toFloat())
//        builder.addStroke(StrokeDescription(path, 0, 300))
//        val gestureDescription = builder.build()
//        val result = dispatchGesture(gestureDescription, object : GestureResultCallback() {
//            override fun onCompleted(gestureDescription: GestureDescription) {
//                super.onCompleted(gestureDescription)
//                Toast.makeText(this@ClientService, "Click compeleted", Toast.LENGTH_SHORT).show()
//            }
//
//            override fun onCancelled(gestureDescription: GestureDescription) {
//                super.onCancelled(gestureDescription)
//                Log.d("nhatnhat", "onStartCommand: $gestureDescription")
//            }
//        }, null)
//        Log.d("nhatnhat", "onStartCommand:3$result")
//    }

//    private fun checkRoomNumber(bitmap: Bitmap) {
//        val bitmapForOcr = getNumberRoomBitmap(bitmap)
//            TextCollectionManager.getInstance().getRoomNumber(bitmapForOcr, object : TextCollectionManager.IGetNumberListener {
//                override fun OnGetNumberSuccess(text: String?) {
//                    bitmapForOcr.recycle()
//                    Log.d("getRoomNumber", "onSuccess: $text")
//                    var roomnumber = text?.intOrString()
//                    if(roomnumber is Int){
//                        Log.d("getRoomNumber", "onSuccess: $roomnumber")
//                        ServerClientDataManager.getInstance().RegisterClientToRoom(baseContext,roomnumber)
//                    }else{
//                        //
//                    }
//                    captureManager?.takeScreenshot()
//
//                }
//
//                override fun OnGetNumberFailed(error: String?) {
//                    bitmapForOcr.recycle()
//                    Log.d("getRoomNumber", "error: $error")
//                    captureManager?.takeScreenshot()
//                }
//
//            })
////        TextCollectionManager.getInstance().get(bitmapForOcr) { currentPosition: CurrentPosition, postionClick: IntArray? ->
////            bitmapForOcr.recycle()
////            if (currentPosition == CurrentPosition.PLaying) {
////                prepareAndPutData(bitmap)
////            } else captureManager!!.takeScreenshot()
////        }
//    }

    private fun getNumberRoomBitmap(bitmap: Bitmap): Bitmap {
            val numberRoomBitmap = Bitmap.createBitmap(bitmap, numberRoomRect.left, numberRoomRect.top, numberRoomRect.right - numberRoomRect.left, numberRoomRect.bottom - numberRoomRect.top)
            bitmap.recycle()
            return numberRoomBitmap
    }

//    private fun prepareAndPutData(bitmap: Bitmap) {
//        val cardsInHandZone = Rect()
//        val listCardsInHand: MutableList<CardBase64> = ArrayList()
//        /**
//         * Chỉnh Bitmap về kích thước hợp lý nhất
//         */
//        scaleRatio = bitmap.width * 1f / bitmap.height * 1f
//        newHeight = bitmap.height
//        newWidth = bitmap.width
//        if (scaleRatio > 0) {
//            newWidth = 1024
//            newHeight = (newHeight / scaleRatio).toInt()
//        }
//        val newBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false)
//
//        /**
//         * Cắt nhỏ Bitmap về khu vực lá bài trên tay
//         */
//        val handCardsBitmap = Bitmap.createBitmap(newBitmap, 0, newHeight - newHeight / 3, newWidth, newHeight / 3)
//        bitmap.recycle()
//        newBitmap.recycle()
//        listCardsInHand.addAll(CardCollectionManager.getInstance().getCardsZoneBitmap(this, handCardsBitmap, cardsInHandZone, 220, 230))
//        handCardsBitmap.recycle()
//        if (listCardsInHand.size > 0) {
////                        if(listCardInHand.size() ==0){
////                            listCardInHand.addAll(listCardsInHand);
////                            ServerClientDataManager.getInstance().putClientHandCards(listCardInHand, new ServerClientDataManager.IClientPutValueListener() {
////                                @Override
////                                public void OnClientPutValueSuccess() {
////                                    captureManager.takeScreenshot();
////                                }
////
////                                @Override
////                                public void OnClientPutValueFail(String error) {
////                                    Toast.makeText(ClientService.this, ""+error, Toast.LENGTH_SHORT).show();
////                                }
////                            });
////                        }else {
//            if (listCardsInHand.size == listCardInHand.size) {
////                                boolean notMatch=false;
////                                for (int i = 0; i <listCardsInHand.size() ; i++) {
////                                    if(!listCardsInHand.get(i).getCardRect().equals(listCardInHand.get(i).getCardLevel())||
////                                            !listCardsInHand.get(i).getCardsuit().equals(listCardInHand.get(i).getCardsuit())||
////                                            !listCardsInHand.get(i).getCardRect().equals(listCardInHand.get(i).getCardRect())){
////                                        notMatch=true;
////                                        break;
////                                    }
////                                }
////                                if(notMatch){
////                                    ServerClientDataManager.getInstance().putClientHandCards(listCardInHand, new ServerClientDataManager.IClientPutValueListener() {
////                                        @Override
////                                        public void OnClientPutValueSuccess() {
////                                            captureManager.takeScreenshot();
////                                        }
////
////                                        @Override
////                                        public void OnClientPutValueFail(String error) {
////                                            Toast.makeText(ClientService.this, "" + error, Toast.LENGTH_SHORT).show();
////                                        }
////                                    });
////                                }else {
//                captureManager!!.takeScreenshot()
//                //                                }
//            } else {
//                listCardInHand.clear()
//                listCardInHand.addAll(listCardsInHand)
//                getInstance().putClientHandCards(listCardInHand, object : IClientPutValueListener {
//                    override fun OnClientPutValueSuccess() {
//                        captureManager!!.takeScreenshot()
//                    }
//
//                    override fun OnClientPutValueFail(error: String) {
//                        Toast.makeText(this@ClientService, "" + error, Toast.LENGTH_SHORT).show()
//                    }
//                })
//            }
//        } else captureManager!!.takeScreenshot()
//    }


}