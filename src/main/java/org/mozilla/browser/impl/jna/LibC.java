package org.mozilla.browser.impl.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public interface LibC extends Library {

    LibC libc = (LibC)
        Native.loadLibrary("c", LibC.class); //$NON-NLS-1$

    public static final int SIGHUP  =  1; //Hangup detected on controlling terminal or death of controlling process
    public static final int SIGINT  =  2; //Interrupt from keyboard
    public static final int SIGQUIT =  3; //Quit from keyboard
    public static final int SIGILL  =  4; //Illegal Instruction
    public static final int SIGABRT =  6; //Abort signal from abort(3)
    public static final int SIGFPE  =  8; //Floating point exception
    public static final int SIGKILL =  9; //Kill signal
    public static final int SIGSEGV = 11; //Invalid memory reference
    public static final int SIGPIPE = 13; //Broken pipe: write to pipe with no readers
    public static final int SIGALRM = 14; //Timer signal from alarm(2)
    public static final int SIGTERM = 15; //Termination signal
    public static final int SIGUSR1 = 10; //User-defined signal 1
    public static final int SIGUSR2 = 12; //User-defined signal 2
    public static final int SIGCHLD = 17; //Child stopped or terminated
    public static final int SIGCONT = 18; //Continue if stopped
    public static final int SIGSTOP = 19; //Stop process
    public static final int SIGTSTP = 20; //Stop typed at tty
    public static final int SIGTTIN = 21; //tty input for background process
    public static final int SIGTTOU = 22; //tty output for background process

    public static final int SIG_DFL =  0; // default signal handling
    public static final int SIG_IGN =  1; // ignore signal
    public static final int SIG_ERR = -1; // error return from signal


    int sigaction(int signum, SigAction newSa, SigAction prevSa);

    public static class SigAction extends Structure {
//        public static interface HandlerFunc extends Callback {
//            void callback(int a1);
//        }
//        public static interface SigActionFunc extends Callback {
//            void callback(int a1, Pointer a2, Pointer a3);
//        }
//        public static interface RestorerFunc extends Callback {
//            void callback();
//        }

        //public HandlerFunc sa_handler;
        //public SigActionFunc sa_sigaction;
        public Pointer sa_handler;
        public Pointer sa_sigaction;
        public SigSet sa_mask;
        public int sa_flags;
        public Pointer sa_restorer;
    }

    public static class SigSet extends Structure {

        public byte
            m000, m001, m002, m003, m004, m005, m006, m007, m008, m009,
            m010, m011, m012, m013, m014, m015, m016, m017, m018, m019,
            m020, m021, m022, m023, m024, m025, m026, m027, m028, m029,
            m030, m031, m032, m033, m034, m035, m036, m037, m038, m039,
            m040, m041, m042, m043, m044, m045, m046, m047, m048, m049,
            m050, m051, m052, m053, m054, m055, m056, m057, m058, m059,
            m060, m061, m062, m063, m064, m065, m066, m067, m068, m069,
            m070, m071, m072, m073, m074, m075, m076, m077, m078, m079,
            m080, m081, m082, m083, m084, m085, m086, m087, m088, m089,
            m090, m091, m092, m093, m094, m095, m096, m097, m098, m099,
            m100, m101, m102, m103, m104, m105, m106, m107, m108, m109,
            m110, m111, m112, m113, m114, m115, m116, m117, m118, m119,
            m120, m121, m122, m123, m124, m125, m126, m127;
    }

    int putenv(String nameeqval);
    int setenv(String name, String value, boolean overwrite);
    String getenv(String name);

}
