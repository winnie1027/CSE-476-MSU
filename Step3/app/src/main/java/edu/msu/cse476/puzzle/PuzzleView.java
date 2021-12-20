package edu.msu.cse476.puzzle;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


/**
 * Custom view class for our Puzzle.
 */
public class PuzzleView extends View {
    /**
     * The actual puzzle
     */
    private Puzzle puzzle;

    public PuzzleView(Context context) {
        super(context);
        init();
    }

    public PuzzleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PuzzleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public Puzzle getPuzzle() {
        return puzzle;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return puzzle.onTouchEvent(this, event);
    }

    private void init() {
        puzzle = new Puzzle(getContext(), this);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        puzzle.draw(canvas);
    }

    /**
     * Save the puzzle to a bundle
     * @param bundle The bundle we save to
     */
    public void saveInstanceState(Bundle bundle) {
        puzzle.saveInstanceState(bundle);
    }

    /**
     * Load the puzzle from a bundle
     * @param bundle The bundle we save to
     */
    public void loadInstanceState(Bundle bundle) {
        puzzle.loadInstanceState(bundle);
    }
}