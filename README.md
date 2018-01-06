# K-Manager

K-Manager builds the substratum app - K-Klock.
You can customize your **statusbar clock** as well as other UI elements in your phone!

### Features:
+ Change the clock color, format, position, font
+ Decide whether the clock should appear on lockscreen or not
+ Move Network Statusbar icons to the left
+ Add custom text to the statusbar, or just to the lockscreen
+ Customize the quick settings panel
+ Input your own colors or formats, or choose from the presets

## Supported Roms

Every Rom needs rom-specific files to work with K-Manager. However, your Rom doesn't need to be supported to try K-Manager.
Follow the guide inside K-Manager to give K-Manager the rom-specific files from your Rom.
*You can enable additional options within K-Manager Settings for more functionality. These may not work on every Rom*

### Adding your Rom to K-Manager

Once you have tried the Other Roms option and it worked on your Rom, you can do this!

+ Zip the userInput folder where you put your rom-specific files
+ Rename this zip file to 'Your Rom Name Android Version'
+ You can either **send the zip to me** or submit a **pull request** to have those files added to K-Manager

    Place the zip file to *app/src/main/assets/romSpecific* if you are making a pull request

+ If you have tried the addition option in K-Manager Settings, you will need to specify these to have the enabled

    If you are making a pull request, edit *app/src/main/res/rom_instructions.xml* and add your zip name to the string-arrays of the options.
    **Make sure to write your zip name just without the .zip part**

+ And that's it!, Your rom will be there in the next update!