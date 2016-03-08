package geert.berkers.primedice.Thread;

import geert.berkers.primedice.Activity.MainActivity;
import geert.berkers.primedice.Data.Bet;

/**
 * Primedice Application Created by Geert on 26-2-2016.
 */
public class AutomatedBetThread {

    private boolean shouldStop = false;
    private int betAmount;
    private final int startAmount;
    private final String target;
    private final String condition;
    private int numberOfRolls;
    private final boolean increaseOnWin;
    private final boolean increaseOnLoss;
    private final double increaseOnWinValue;
    private final double increaseOnLossValue;
    private final MainActivity activity;

    private boolean previousBetMade = false;

    public AutomatedBetThread(MainActivity activity, int startAmount, String target, String condition, int numberOfRolls, boolean increaseOnWin, boolean increaseOnLoss, double increaseOnWinValue, double increaseOnLossValue) {
        this.activity = activity;
        this.betAmount = startAmount;
        this.startAmount = startAmount;
        this.target = target;
        this.condition = condition;
        this.numberOfRolls = numberOfRolls;
        this.increaseOnWin = increaseOnWin;
        this.increaseOnLoss = increaseOnLoss;
        this.increaseOnWinValue = increaseOnWinValue;
        this.increaseOnLossValue = increaseOnLossValue;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public void startBetting() {
        while (true) {
            try {
                // Check if thread has to stop.
                if (shouldStop) {
                    return;
                } else if (numberOfRolls == 0) {
                    activity.stopAutomatedBetThread();
                } else {
                    Bet bet = activity.makeBet(betAmount, target, condition);

                    if(bet != null) {
                        boolean betWon = bet.getWinOrLose();

                        if (betWon) {
                            if (increaseOnWin) {
                                betAmount += betAmount * increaseOnWinValue / 100;
                            } else {
                                betAmount = startAmount;
                            }
                        } else { // Bet lost
                            if (increaseOnLoss) {
                                betAmount += betAmount * increaseOnLossValue / 100;
                            } else {
                                betAmount = startAmount;
                            }
                        }
                    }
                    if (numberOfRolls != -1) {
                        numberOfRolls--;
                    }

                    while (!previousBetMade) {
                        // Wait until last bet is made
                        // Else application could freeze
                    }
                }

                // Max 2 bets in 1 second so wait a half second.
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void requestStop() {
        shouldStop = true;
    }

    public void betMade(boolean betMade) {
        previousBetMade = betMade;
    }
}
