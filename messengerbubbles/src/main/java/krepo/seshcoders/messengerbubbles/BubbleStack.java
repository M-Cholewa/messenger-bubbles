package krepo.seshcoders.messengerbubbles;

import java.util.ArrayList;
import java.util.List;

public class BubbleStack implements OnMainBubbleActionListener {

    private List<BubbleLayout> bubbles = new ArrayList<>();
    private List<BubbleLayout> pendingBubbles = new ArrayList<>();
    private boolean isInitialised = false;
    private boolean isStackInMotion = false;
    private boolean shouldStickToWall = false;
    private int maxStackSize = 3;
    private BubblesService bubblesService;
    private int xPos = 0;
    private int yPos = 100;

    @Override
    public void onMainBubbleMove(int x, int y, float animationTime) {
        followMainBubble(x, y, animationTime);
        isStackInMotion = true;
    }

    @Override
    public void onMainBubbleThrashed() {
        // TODO: 06.02.2020  onMainBubbleThrashed
    }

    @Override
    public void onMainBubbleAnimationFinish() {
        if (shouldStickToWall) {
            for (int i = 0; i < bubbles.size(); i++) {
                if (i == 0) continue;
                BubbleLayout bubble = bubbles.get(i);
                bubble.goToWall(null, bubble.getStackPosition()*150);
            }
            shouldStickToWall = false;
        }

    }

    @Override
    public void onMainBubbleStickToWall(int x, int y, MessCloudView.BubbleCurrentWall wall) {
        shouldStickToWall = true;
        isStackInMotion = false;
        for (int i = 0; i < bubbles.size(); i++) {
            if (i == 0) continue;
            BubbleLayout bubble = bubbles.get(i);
            bubble.notifyStickToWall(wall);
        }
        setXYPos(x, y);
    }

    @Override
    public void onMessageDisplay(BubbleLayout bubbleLayout) {
        // TODO: 27.01.2020 message display 
    }

    public synchronized void addBubble(BubbleLayout... layouts) {
        if (bubbles.size() >= maxStackSize && !bubbles.isEmpty()) {
            removeBubble(bubbles.get(maxStackSize - 1));
        }
        for (BubbleLayout bubble : layouts) {
            bubble.setOnMainBubbleActionListener(this);
            if (isInitialised) {
                bubble.setStackPosition(bubbles.size());
                setMainBubble(bubble);
                bubblesService.addBubble(bubble, xPos, yPos);
            } else {
                pendingBubbles.add(bubble);
            }
        }
    }

    public void removeBubble(BubbleLayout layout) {
        bubblesService.removeBubble(layout);
        bubbles.remove(layout);
        if (layout.getStackPosition() == 0)
            setMainBubble(bubbles.get(1));
    }

    private void followMainBubble(int x, int y, float animTime) {
        for (BubbleLayout bubbleLayout : bubbles) {
            if (bubbleLayout.getStackPosition() == 0)
                continue;
            float followAnimTime;
            
            if (animTime == 0f) {
                followAnimTime = bubbleLayout.getStackPosition() == 1
                    ? 40f
                    : 60f;
            } else {
                followAnimTime =
                        animTime*1.25f + (animTime *
                                ((float)bubbleLayout.getStackPosition() / (float)bubbles.size())
                                );
            }
            bubbleLayout.getAnimator().stop();
            bubbleLayout.getAnimator().start(x, y, followAnimTime);
        }
        setXYPos(x, y);
    }

    void initialiseStack() {
        isInitialised = true;
        addBubble(pendingBubbles.toArray(
                new BubbleLayout[pendingBubbles.size()]
        ));
    }

    //getters and setters
    void setXYPos(int xPos, int yPos) {
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public void setMaxStackSize(int maxStackSize) {
        this.maxStackSize = maxStackSize;
    }

    void setBubblesService(BubblesService service) {
        this.bubblesService = service;
    }

    public BubbleLayout getMainBubble() {
        return bubbles.get(0);
    }

    public void setMainBubble(BubbleLayout mainBubble) {
        if (bubbles.contains(mainBubble)) {
            for (BubbleLayout layout : bubbles) {
                if (layout.getStackPosition() < mainBubble.getStackPosition()) {
                    layout.setStackPosition(layout.getStackPosition() + 1);
                }
            }
            bubbles.remove(mainBubble);
            mainBubble.getWindowManager().removeView(mainBubble);
            mainBubble.getWindowManager().addView(mainBubble, mainBubble.getLayoutParams());
        } else {
            for (BubbleLayout layout : bubbles) {
                layout.setStackPosition(layout.getStackPosition() + 1);
            }
        }
        bubbles.add(0, mainBubble);
        mainBubble.setStackPosition(0);
    }

    public int getxPos() {
        return xPos;
    }

    public int getyPos() {
        return yPos;
    }


}
