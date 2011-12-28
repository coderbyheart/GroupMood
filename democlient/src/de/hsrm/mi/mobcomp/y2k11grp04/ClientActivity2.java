//import android.view.MotionEvent;
//import android.view.View;
//
/////**
//// * 
//// */
////package de.hsrm.mi.mobcomp.y2k11grp04;
////
/////**
//// * @author Coralie Reuter
//// * 
//// */
////public class ClientActivity2 {
////	private void changeSlide(int i) {
////		switch (i) {
////		case 1:
////			if (currentSlide == slideIDs.size() - 1) {
////				currentSlide = -1;
////			}
////			currentSlide += 1;
////
////			break;
////		case -1:
////			if (currentSlide == 0) {
////				currentSlide = slideIDs.size();
////			}
////			currentSlide -= 1;
////			break;
////		}
////		iswitch.setImageResource(slideIDs.get(currentSlide + i));
////
////	}
////
//// }
//	private float downX;
//	private float upX;
//	private float downY;
//	private float upY;
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see android.view.View.OnTouchListener#onTouch(android.view.View,
//	 * android.view.MotionEvent)
//	 */
//	@Override
//	public boolean onTouch(View view, MotionEvent motionEvent) {
//
//		switch (motionEvent.getAction()) {
//		case MotionEvent.ACTION_DOWN:
//			downX = motionEvent.getX();
//			downY = motionEvent.getY();
//			break;
//
//		case MotionEvent.ACTION_UP:
//			upX = motionEvent.getX();
//			upY = motionEvent.getY();
//
//			float dif = Math.abs(downY - upY);
//
//			if (dif < 50 && downX < upX) {
//				changeSlide(-1);
//			}
//			if (dif < 50 && downX > upX) {
//				changeSlide(1);
//
//				break;
//			}
//
//		}
//		return false;
//
// }
// private final int currentSlide = 0;
