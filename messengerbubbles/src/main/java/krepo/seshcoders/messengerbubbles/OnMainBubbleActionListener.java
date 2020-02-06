package krepo.seshcoders.messengerbubbles;

public interface OnMainBubbleActionListener {
    void onMainBubbleMove(int x, int y, float animationTime);
    void onMainBubbleStickToWall(int x, int y, MessCloudView.BubbleCurrentWall wall);
    void onMainBubbleAnimationFinish();
    void onMainBubbleThrashed();
    void onMessageDisplay(BubbleLayout bubbleLayout);
}
