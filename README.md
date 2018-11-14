# AutoChair

AutoChair is an entrepreneurial proof of concept for a money-saving-yet-luxurious-and-eco-friendly smart heated and cooled chair.

The concept and prototype are summarized in this [presentation](https://docs.google.com/presentation/d/1PwM7oFpEUKkRi-hhpl7nUTeb2gQ6eTHOeNojYia640A/edit?usp=sharing). (Check the slides' notes for more information.)

The concept is pitched in more detail in this [proposal](https://drive.google.com/file/d/1iZCnCp_j40yasH4KtBz-Mgz2zKYmuGju/view?usp=sharing).

## Prototype Features
* switch between 3 profiles
  * a "no tag" one that doesn't synchronize or remember settings between uses
  * two tagged profiles that do synchronize
    * synchronized settings will persist across even complete deletion and recreation of the AVD
* adjust temperature in all three profiles
  * observe as that changes the displayed temperature
    * changes color at certain temperature intervals
    * bound to a 40-95 degree (Fahrenheit) range
* adjusting ambient temperature sensors (in AVD settings) will also change the number displayed for ambient temperature in the app
  * when switching tags, observe that the remembered temperature is modified based on the ambient temperature sensed
* if running on a real device, physical NFC tags can be used instead of the profiles



