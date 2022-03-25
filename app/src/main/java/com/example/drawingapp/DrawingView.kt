package com.example.drawingapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import java.util.ArrayList
import java.util.jar.Attributes

class DrawingView(context:Context,attrs:AttributeSet):View(context,attrs) {
    private var mDrawPath:CustomPath? = null
    private var mCanvasBitmap:Bitmap?=null
    private var mDrawPaint: Paint?=null
    private var mCanvasPaint:Paint?=null
    var isEraser:Boolean?=null
    private var  mBrushSize:Float=0.toFloat()
    private var color=Color.BLACK
    private var canvas:Canvas?=null
    private var mUndoPaths=ArrayList<CustomPath>()
    private val mPath=ArrayList<CustomPath>()

    init {
        setUpDrawing()
    }
    fun undo(){
        if(mPath.size>0)
        {
            mUndoPaths.add(mPath.removeAt(mPath.size-1))
            invalidate()
        }
    }
    fun redo(){
        if(mUndoPaths.size>0){
            mPath.add(mUndoPaths.removeAt(mUndoPaths.size-1))
            invalidate()
        }
    }
    private fun setUpDrawing()
    {
        mDrawPaint= Paint()
        mDrawPath=CustomPath(color,mBrushSize)
        mDrawPaint!!.color=color
        mDrawPaint!!.style=Paint.Style.STROKE
        mDrawPaint!!.strokeJoin=Paint.Join.ROUND
        mDrawPaint!!.strokeCap=Paint.Cap.ROUND
        mCanvasPaint= Paint(Paint.DITHER_FLAG)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap= Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        canvas=Canvas(mCanvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mCanvasBitmap!!,0f,0f,mCanvasPaint)
        for(path in mPath)
        {
            mDrawPaint!!.strokeWidth=path.brushThickness
            mDrawPaint!!.color=path.color
            canvas.drawPath(path,mDrawPaint!!)
        }
        if(!mDrawPath!!.isEmpty)
        {
            mDrawPaint!!.strokeWidth=mDrawPath!!.brushThickness
            mDrawPaint!!.color=mDrawPath!!.color
            canvas.drawPath(mDrawPath!!,mDrawPaint!!)

        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX=event?.x
        val touchY=event?.y
        when(event?.action)
        {
            MotionEvent.ACTION_DOWN->{
                mDrawPath!!.color=color
                mDrawPath!!.brushThickness=mBrushSize
                mDrawPath!!.reset()
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.moveTo(touchX,touchY)
                    }
                }
            }
            MotionEvent.ACTION_MOVE->{
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.lineTo(touchX,touchY)
                    }
                }

            }
            MotionEvent.ACTION_UP->{
                mPath.add(mDrawPath!!)
                mDrawPath=CustomPath(color,mBrushSize)
            }
            else -> return false
        }
        invalidate()
        return true
    }
    fun setBrushSize(newSize:Float)
    {
        mBrushSize=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,newSize,resources.displayMetrics)
        mDrawPaint!!.strokeWidth=mBrushSize
    }
    fun setColor(newColor:String)
    {
        color=Color.parseColor(newColor)
        mDrawPaint!!.color=color
    }
    /*fun erase()
    {
        mDrawPaint!!.alpha=0
        color=Color.TRANSPARENT
        mDrawPaint!!.setColor(Color.TRANSPARENT)
        mDrawPaint!!.strokeWidth=mBrushSize
        mDrawPaint!!.style=Paint.Style.STROKE
        mDrawPaint!!.setMaskFilter(null)
        mDrawPaint!!.setXfermode(PorterDuffXfermode(PorterDuff.Mode.CLEAR))
        mDrawPath!!.reset()
    }*/
    
    internal inner class CustomPath(var color:Int
    ,var brushThickness:Float): Path(){

    }




}