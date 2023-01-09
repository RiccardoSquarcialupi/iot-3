#ifndef __TASK__
#define __TASK__

class Task {
  public:
    virtual void init(int taskPeriod, int basePeriod) {
      this->period = taskPeriod;
      this->basePeriod = basePeriod;
      timeElapsed = 0;
    }

    bool updateAndCheckTime() {
      timeElapsed += basePeriod;
      t += basePeriod;
      if (timeElapsed >= period) {
        timeElapsed = 0;
        this->tick();
        return true;
      }

      return false;
    }

    virtual void tick() = 0;
    int t = 0;
  private:
    int basePeriod;
    int timeElapsed;
  protected:
    int period;
};
#endif
