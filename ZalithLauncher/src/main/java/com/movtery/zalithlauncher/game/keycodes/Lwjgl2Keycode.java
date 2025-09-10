package com.movtery.zalithlauncher.game.keycodes;

public class Lwjgl2Keycode {
    public static final int KEY_ESCAPE = 1;
    public static final int KEY_1 = 2;
    public static final int KEY_2 = 3;
    public static final int KEY_3 = 4;
    public static final int KEY_4 = 5;
    public static final int KEY_5 = 6;
    public static final int KEY_6 = 7;
    public static final int KEY_7 = 8;
    public static final int KEY_8 = 9;
    public static final int KEY_9 = 10;
    public static final int KEY_0 = 11;
    public static final int KEY_MINUS = 12;
    public static final int KEY_EQUALS = 13;
    public static final int KEY_BACK = 14;
    public static final int KEY_TAB = 15;
    public static final int KEY_Q = 16;
    public static final int KEY_W = 17;
    public static final int KEY_E = 18;
    public static final int KEY_R = 19;
    public static final int KEY_T = 20;
    public static final int KEY_Y = 21;
    public static final int KEY_U = 22;
    public static final int KEY_I = 23;
    public static final int KEY_O = 24;
    public static final int KEY_P = 25;
    public static final int KEY_LBRACKET = 26;
    public static final int KEY_RBRACKET = 27;
    public static final int KEY_RETURN = 28;
    public static final int KEY_LCONTROL = 29;
    public static final int KEY_A = 30;
    public static final int KEY_S = 31;
    public static final int KEY_D = 32;
    public static final int KEY_F = 33;
    public static final int KEY_G = 34;
    public static final int KEY_H = 35;
    public static final int KEY_J = 36;
    public static final int KEY_K = 37;
    public static final int KEY_L = 38;
    public static final int KEY_SEMICOLON = 39;
    public static final int KEY_APOSTROPHE = 40;
    public static final int KEY_GRAVE = 41;
    public static final int KEY_LSHIFT = 42;
    public static final int KEY_BACKSLASH = 43;
    public static final int KEY_Z = 44;
    public static final int KEY_X = 45;
    public static final int KEY_C = 46;
    public static final int KEY_V = 47;
    public static final int KEY_B = 48;
    public static final int KEY_N = 49;
    public static final int KEY_M = 50;
    public static final int KEY_COMMA = 51;
    public static final int KEY_PERIOD = 52;
    public static final int KEY_SLASH = 53;
    public static final int KEY_RSHIFT = 54;
    public static final int KEY_MULTIPLY = 55;
    public static final int KEY_LMENU = 56;
    public static final int KEY_SPACE = 57;
    public static final int KEY_CAPITAL = 58;
    public static final int KEY_F1 = 59;
    public static final int KEY_F2 = 60;
    public static final int KEY_F3 = 61;
    public static final int KEY_F4 = 62;
    public static final int KEY_F5 = 63;
    public static final int KEY_F6 = 64;
    public static final int KEY_F7 = 65;
    public static final int KEY_F8 = 66;
    public static final int KEY_F9 = 67;
    public static final int KEY_F10 = 68;
    public static final int KEY_NUMLOCK = 69;
    public static final int KEY_SCROLL = 70;
    public static final int KEY_NUMPAD7 = 71;
    public static final int KEY_NUMPAD8 = 72;
    public static final int KEY_NUMPAD9 = 73;
    public static final int KEY_SUBTRACT = 74;
    public static final int KEY_NUMPAD4 = 75;
    public static final int KEY_NUMPAD5 = 76;
    public static final int KEY_NUMPAD6 = 77;
    public static final int KEY_ADD = 78;
    public static final int KEY_NUMPAD1 = 79;
    public static final int KEY_NUMPAD2 = 80;
    public static final int KEY_NUMPAD3 = 81;
    public static final int KEY_NUMPAD0 = 82;
    public static final int KEY_DECIMAL = 83;
    public static final int KEY_F11 = 87;
    public static final int KEY_F12 = 88;
    public static final int KEY_F13 = 100;
    public static final int KEY_F14 = 101;
    public static final int KEY_F15 = 102;
    public static final int KEY_F16 = 103;
    public static final int KEY_F17 = 104;
    public static final int KEY_F18 = 105;
    public static final int KEY_KANA = 112;
    public static final int KEY_F19 = 113;
    public static final int KEY_CONVERT = 121;
    public static final int KEY_NOCONVERT = 123;
    public static final int KEY_YEN = 125;
    public static final int KEY_NUMPADEQUALS = 141;
    public static final int KEY_CIRCUMFLEX = 144;
    public static final int KEY_AT = 145;
    public static final int KEY_COLON = 146;
    public static final int KEY_UNDERLINE = 147;
    public static final int KEY_KANJI = 148;
    public static final int KEY_STOP = 149;
    public static final int KEY_AX = 150;
    public static final int KEY_UNLABELED = 151;
    public static final int KEY_NUMPADENTER = 156;
    public static final int KEY_RCONTROL = 157;
    public static final int KEY_SECTION = 167;
    public static final int KEY_NUMPADCOMMA = 179;
    public static final int KEY_DIVIDE = 181;
    public static final int KEY_SYSRQ = 183;
    public static final int KEY_RMENU = 184;
    public static final int KEY_FUNCTION = 196;
    public static final int KEY_PAUSE = 197;
    public static final int KEY_HOME = 199;
    public static final int KEY_UP = 200;
    public static final int KEY_PRIOR = 201;
    public static final int KEY_LEFT = 203;
    public static final int KEY_RIGHT = 205;
    public static final int KEY_END = 207;
    public static final int KEY_DOWN = 208;
    public static final int KEY_NEXT = 209;
    public static final int KEY_INSERT = 210;
    public static final int KEY_DELETE = 211;
    public static final int KEY_CLEAR = 218;
    public static final int KEY_LMETA = 219;
    @Deprecated
    public static final int KEY_LWIN = 219;
    public static final int KEY_RMETA = 220;
    @Deprecated
    public static final int KEY_RWIN = 220;
    public static final int KEY_APPS = 221;
    public static final int KEY_POWER = 222;
    public static final int KEY_SLEEP = 223;

    public static int lwjgl2ToGlfw(int k) {
        switch (k) {
            case KEY_ESCAPE: return LwjglGlfwKeycode.GLFW_KEY_ESCAPE;

            case KEY_0: return LwjglGlfwKeycode.GLFW_KEY_0;
            case KEY_1: return LwjglGlfwKeycode.GLFW_KEY_1;
            case KEY_2: return LwjglGlfwKeycode.GLFW_KEY_2;
            case KEY_3: return LwjglGlfwKeycode.GLFW_KEY_3;
            case KEY_4: return LwjglGlfwKeycode.GLFW_KEY_4;
            case KEY_5: return LwjglGlfwKeycode.GLFW_KEY_5;
            case KEY_6: return LwjglGlfwKeycode.GLFW_KEY_6;
            case KEY_7: return LwjglGlfwKeycode.GLFW_KEY_7;
            case KEY_8: return LwjglGlfwKeycode.GLFW_KEY_8;
            case KEY_9: return LwjglGlfwKeycode.GLFW_KEY_9;

            case KEY_A: return LwjglGlfwKeycode.GLFW_KEY_A;
            case KEY_B: return LwjglGlfwKeycode.GLFW_KEY_B;
            case KEY_C: return LwjglGlfwKeycode.GLFW_KEY_C;
            case KEY_D: return LwjglGlfwKeycode.GLFW_KEY_D;
            case KEY_E: return LwjglGlfwKeycode.GLFW_KEY_E;
            case KEY_F: return LwjglGlfwKeycode.GLFW_KEY_F;
            case KEY_G: return LwjglGlfwKeycode.GLFW_KEY_G;
            case KEY_H: return LwjglGlfwKeycode.GLFW_KEY_H;
            case KEY_I: return LwjglGlfwKeycode.GLFW_KEY_I;
            case KEY_J: return LwjglGlfwKeycode.GLFW_KEY_J;
            case KEY_K: return LwjglGlfwKeycode.GLFW_KEY_K;
            case KEY_L: return LwjglGlfwKeycode.GLFW_KEY_L;
            case KEY_M: return LwjglGlfwKeycode.GLFW_KEY_M;
            case KEY_N: return LwjglGlfwKeycode.GLFW_KEY_N;
            case KEY_O: return LwjglGlfwKeycode.GLFW_KEY_O;
            case KEY_P: return LwjglGlfwKeycode.GLFW_KEY_P;
            case KEY_Q: return LwjglGlfwKeycode.GLFW_KEY_Q;
            case KEY_R: return LwjglGlfwKeycode.GLFW_KEY_R;
            case KEY_S: return LwjglGlfwKeycode.GLFW_KEY_S;
            case KEY_T: return LwjglGlfwKeycode.GLFW_KEY_T;
            case KEY_U: return LwjglGlfwKeycode.GLFW_KEY_U;
            case KEY_V: return LwjglGlfwKeycode.GLFW_KEY_V;
            case KEY_W: return LwjglGlfwKeycode.GLFW_KEY_W;
            case KEY_X: return LwjglGlfwKeycode.GLFW_KEY_X;
            case KEY_Y: return LwjglGlfwKeycode.GLFW_KEY_Y;
            case KEY_Z: return LwjglGlfwKeycode.GLFW_KEY_Z;

            case KEY_MINUS: return LwjglGlfwKeycode.GLFW_KEY_MINUS;
            case KEY_EQUALS: return LwjglGlfwKeycode.GLFW_KEY_EQUAL;
            case KEY_LBRACKET: return LwjglGlfwKeycode.GLFW_KEY_LEFT_BRACKET;
            case KEY_RBRACKET: return LwjglGlfwKeycode.GLFW_KEY_RIGHT_BRACKET;
            case KEY_SEMICOLON: return LwjglGlfwKeycode.GLFW_KEY_SEMICOLON;
            case KEY_APOSTROPHE: return LwjglGlfwKeycode.GLFW_KEY_APOSTROPHE;
            case KEY_GRAVE: return LwjglGlfwKeycode.GLFW_KEY_GRAVE_ACCENT;
            case KEY_BACKSLASH: return LwjglGlfwKeycode.GLFW_KEY_BACKSLASH;
            case KEY_COMMA: return LwjglGlfwKeycode.GLFW_KEY_COMMA;
            case KEY_PERIOD: return LwjglGlfwKeycode.GLFW_KEY_PERIOD;
            case KEY_SLASH: return LwjglGlfwKeycode.GLFW_KEY_SLASH;

            case KEY_SPACE: return LwjglGlfwKeycode.GLFW_KEY_SPACE;
            case KEY_TAB: return LwjglGlfwKeycode.GLFW_KEY_TAB;
            case KEY_RETURN: return LwjglGlfwKeycode.GLFW_KEY_ENTER;
            case KEY_BACK: return LwjglGlfwKeycode.GLFW_KEY_BACKSPACE;
            case KEY_INSERT: return LwjglGlfwKeycode.GLFW_KEY_INSERT;
            case KEY_DELETE: return LwjglGlfwKeycode.GLFW_KEY_DELETE;
            case KEY_HOME: return LwjglGlfwKeycode.GLFW_KEY_HOME;
            case KEY_END: return LwjglGlfwKeycode.GLFW_KEY_END;
            case KEY_PRIOR: return LwjglGlfwKeycode.GLFW_KEY_PAGE_UP;   // PageUp
            case KEY_NEXT: return LwjglGlfwKeycode.GLFW_KEY_PAGE_DOWN;  // PageDown

            case KEY_UP: return LwjglGlfwKeycode.GLFW_KEY_UP;
            case KEY_DOWN: return LwjglGlfwKeycode.GLFW_KEY_DOWN;
            case KEY_LEFT: return LwjglGlfwKeycode.GLFW_KEY_LEFT;
            case KEY_RIGHT: return LwjglGlfwKeycode.GLFW_KEY_RIGHT;

            case KEY_F1: return LwjglGlfwKeycode.GLFW_KEY_F1;
            case KEY_F2: return LwjglGlfwKeycode.GLFW_KEY_F2;
            case KEY_F3: return LwjglGlfwKeycode.GLFW_KEY_F3;
            case KEY_F4: return LwjglGlfwKeycode.GLFW_KEY_F4;
            case KEY_F5: return LwjglGlfwKeycode.GLFW_KEY_F5;
            case KEY_F6: return LwjglGlfwKeycode.GLFW_KEY_F6;
            case KEY_F7: return LwjglGlfwKeycode.GLFW_KEY_F7;
            case KEY_F8: return LwjglGlfwKeycode.GLFW_KEY_F8;
            case KEY_F9: return LwjglGlfwKeycode.GLFW_KEY_F9;
            case KEY_F10: return LwjglGlfwKeycode.GLFW_KEY_F10;
            case KEY_F11: return LwjglGlfwKeycode.GLFW_KEY_F11;
            case KEY_F12: return LwjglGlfwKeycode.GLFW_KEY_F12;
            case KEY_F13: return LwjglGlfwKeycode.GLFW_KEY_F13;
            case KEY_F14: return LwjglGlfwKeycode.GLFW_KEY_F14;
            case KEY_F15: return LwjglGlfwKeycode.GLFW_KEY_F15;
            case KEY_F16: return LwjglGlfwKeycode.GLFW_KEY_F16;
            case KEY_F17: return LwjglGlfwKeycode.GLFW_KEY_F17;
            case KEY_F18: return LwjglGlfwKeycode.GLFW_KEY_F18;
            case KEY_F19: return LwjglGlfwKeycode.GLFW_KEY_F19;

            case KEY_LSHIFT: return LwjglGlfwKeycode.GLFW_KEY_LEFT_SHIFT;
            case KEY_RSHIFT: return LwjglGlfwKeycode.GLFW_KEY_RIGHT_SHIFT;
            case KEY_LCONTROL: return LwjglGlfwKeycode.GLFW_KEY_LEFT_CONTROL;
            case KEY_RCONTROL: return LwjglGlfwKeycode.GLFW_KEY_RIGHT_CONTROL;
            case KEY_LMENU: return LwjglGlfwKeycode.GLFW_KEY_LEFT_ALT;
            case KEY_RMENU: return LwjglGlfwKeycode.GLFW_KEY_RIGHT_ALT;
            case KEY_LMETA: return LwjglGlfwKeycode.GLFW_KEY_LEFT_SUPER;   // Win / Meta
            case KEY_RMETA: return LwjglGlfwKeycode.GLFW_KEY_RIGHT_SUPER;  // Win / Meta

            case KEY_CAPITAL: return LwjglGlfwKeycode.GLFW_KEY_CAPS_LOCK;
            case KEY_NUMLOCK: return LwjglGlfwKeycode.GLFW_KEY_NUM_LOCK;
            case KEY_SCROLL: return LwjglGlfwKeycode.GLFW_KEY_SCROLL_LOCK;
            case KEY_SYSRQ: return LwjglGlfwKeycode.GLFW_KEY_PRINT_SCREEN;
            case KEY_PAUSE: return LwjglGlfwKeycode.GLFW_KEY_PAUSE;

            case KEY_NUMPAD0: return LwjglGlfwKeycode.GLFW_KEY_KP_0;
            case KEY_NUMPAD1: return LwjglGlfwKeycode.GLFW_KEY_KP_1;
            case KEY_NUMPAD2: return LwjglGlfwKeycode.GLFW_KEY_KP_2;
            case KEY_NUMPAD3: return LwjglGlfwKeycode.GLFW_KEY_KP_3;
            case KEY_NUMPAD4: return LwjglGlfwKeycode.GLFW_KEY_KP_4;
            case KEY_NUMPAD5: return LwjglGlfwKeycode.GLFW_KEY_KP_5;
            case KEY_NUMPAD6: return LwjglGlfwKeycode.GLFW_KEY_KP_6;
            case KEY_NUMPAD7: return LwjglGlfwKeycode.GLFW_KEY_KP_7;
            case KEY_NUMPAD8: return LwjglGlfwKeycode.GLFW_KEY_KP_8;
            case KEY_NUMPAD9: return LwjglGlfwKeycode.GLFW_KEY_KP_9;
            case KEY_DECIMAL: return LwjglGlfwKeycode.GLFW_KEY_KP_DECIMAL;
            case KEY_DIVIDE: return LwjglGlfwKeycode.GLFW_KEY_KP_DIVIDE;
            case KEY_MULTIPLY: return LwjglGlfwKeycode.GLFW_KEY_KP_MULTIPLY;
            case KEY_SUBTRACT: return LwjglGlfwKeycode.GLFW_KEY_KP_SUBTRACT;
            case KEY_ADD: return LwjglGlfwKeycode.GLFW_KEY_KP_ADD;
            case KEY_NUMPADENTER: return LwjglGlfwKeycode.GLFW_KEY_KP_ENTER;
            case KEY_NUMPADEQUALS: return LwjglGlfwKeycode.GLFW_KEY_KP_EQUAL;

            case KEY_APPS: return LwjglGlfwKeycode.GLFW_KEY_MENU;

            case KEY_KANA:
            case KEY_CONVERT:
            case KEY_NOCONVERT:
            case KEY_YEN:
            case KEY_CIRCUMFLEX:
            case KEY_AT:
            case KEY_COLON:
            case KEY_UNDERLINE:
            case KEY_KANJI:
            case KEY_STOP:
            case KEY_AX:
            case KEY_UNLABELED:
            case KEY_SECTION:
            case KEY_NUMPADCOMMA:
            case KEY_POWER:
            case KEY_SLEEP:
            case KEY_FUNCTION:
            case KEY_CLEAR:
            default:
                return LwjglGlfwKeycode.GLFW_KEY_UNKNOWN;
        }
    }
}
