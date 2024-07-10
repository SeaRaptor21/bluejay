import os
import sys

def main():
    os.system("java -cp classes com.esjr.bluejay.Main "+" ".join(sys.argv[1:]))

if __name__ == "__main__":
    main()