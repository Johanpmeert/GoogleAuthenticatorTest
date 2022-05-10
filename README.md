# GoogleAuthenticatorTest
Demonstration of generating Google Authenticator code, it's QR code and the 6 digit timecode

This program does the following:
1. create a new, cryptoghraphic secure random key and show it (20 bytes random key, shown in Base32 format so 32 characters long). For easy reading/typing it is shown in blocks of 4 characters
2. create and show the corresponding QR code compatible with the Google Authenticator app
3. start calculating the timecode and updating it every time it changes, so you can easily doublecheck on your smartphone

You can also call it with the code as an argument, and it returns a single timecode:

java -jar GoogleAuthenticator.jar "6k2x hibg zeo3 zgcv 5sax zd7t xf4y jeer"

or

java -jar GoogleAuthenticator.jar 6k2xhibgzeo3zgcv5saxzd7txf4yjeer