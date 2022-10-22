package com.example.cmd.adapter

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

//TouchHelper для реализации действий при свайпе в RecyclerView
class MyTouchHelper(dpi: Float) : ItemTouchHelper.Callback() {
  private var limit = 96 * dpi
  private var current = 0
  private var currentInactive = 0
  private var inactive = false
  private var initXInactive = 0f
  override fun getMovementFlags(
    recyclerView: RecyclerView,
    viewHolder: RecyclerView.ViewHolder
  ): Int =
    makeMovementFlags(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)


  override fun onMove(
    recyclerView: RecyclerView,
    viewHolder: RecyclerView.ViewHolder,
    target: RecyclerView.ViewHolder
  ): Boolean = true

  override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

  }

  override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float =
    Int.MAX_VALUE.toFloat()

  override fun getSwipeEscapeVelocity(defaultValue: Float): Float = Int.MAX_VALUE.toFloat()

  override fun onChildDraw(
    c: Canvas,
    recyclerView: RecyclerView,
    viewHolder: RecyclerView.ViewHolder,
    dX: Float,
    dY: Float,
    actionState: Int,
    isCurrentlyActive: Boolean
  ) {
    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
      if (dX == 0f) {
        current = viewHolder.itemView.scrollX
        inactive = true
      }
      if (isCurrentlyActive) {
        var scrollOffset = current - dX
        if (scrollOffset > limit) {
          scrollOffset = limit
        } else if (scrollOffset < 0)
          scrollOffset = 0f
        viewHolder.itemView.scrollTo(scrollOffset.toInt(), 0)
      } else {
        if (inactive) {
          inactive = false
          currentInactive = viewHolder.itemView.scrollX
          initXInactive = dX
        }
        if (viewHolder.itemView.scrollX < limit) {
          viewHolder.itemView.scrollTo((currentInactive * dX / initXInactive).toInt(), 0)
        }
      }
    }
  }

  override fun clearView(
    recyclerView: RecyclerView,
    viewHolder: RecyclerView.ViewHolder
  ) {
    super.clearView(recyclerView, viewHolder)
    if (viewHolder.itemView.scrollX > limit) {
      viewHolder.itemView.scrollTo(limit.toInt(), 0)
    }
  }


}
