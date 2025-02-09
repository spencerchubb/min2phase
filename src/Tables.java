package src;

public class Tables {
    // TODO these are redundant in CoordCube and Tables
    static final int N_MOVES = 18;
    static final int N_MOVES2 = 10;
    static final int N_SLICE = 495;
    static final int N_TWIST = 2187;
    static final int N_TWIST_SYM = 324;
    static final int N_FLIP = 2048;
    static final int N_FLIP_SYM = 336;
    static final int N_PERM = 40320;
    static final int N_PERM_SYM = 2768;
    static final int N_MPERM = 24;
    static final int N_COMB = 140;
    static final int P2_PARITY_MOVE = 0xA5;

    /**
     * 0: not initialized, 1: partially initialized, 2: finished
     */
    static int initLevel = 0;

    // phase1
    public static int[][] UDSliceMove = new int[N_SLICE][N_MOVES];
    public static int[][] TwistMove = new int[N_TWIST_SYM][N_MOVES];
    public static int[][] FlipMove = new int[N_FLIP_SYM][N_MOVES];
    public static int[][] UDSliceConj = new int[N_SLICE][8];
    public static int[] UDSliceTwistPrun = new int[N_SLICE * N_TWIST_SYM / 8 + 1];
    public static int[] UDSliceFlipPrun = new int[N_SLICE * N_FLIP_SYM / 8 + 1];
    public static int[] TwistFlipPrun = new int[N_FLIP * N_TWIST_SYM / 8 + 1];

    // phase2
    static int[][] CPermMove = new int[N_PERM_SYM][N_MOVES2];
    static int[][] EPermMove = new int[N_PERM_SYM][N_MOVES2];
    static int[][] MPermMove = new int[N_MPERM][N_MOVES2];
    static int[][] MPermConj = new int[N_MPERM][16];
    static int[][] CCombPMove = new int[N_COMB][N_MOVES2];
    static int[][] CCombPConj = new int[N_COMB][16];
    static int[] MCPermPrun = new int[N_MPERM * N_PERM_SYM / 8 + 1];
    static int[] EPermCCombPPrun = new int[N_COMB * N_PERM_SYM / 8 + 1];

    public Tables() {
    }

    public static void init(boolean fullInit) {
        if (initLevel == 2 || initLevel == 1 && !fullInit) {
            return;
        }
        if (initLevel == 0) {
            CubieCube.initPermSym2Raw();
            initCPermMove();
            initEPermMove();
            initMPermMoveConj();
            initCombPMoveConj();

            CubieCube.initFlipSym2Raw();
            CubieCube.initTwistSym2Raw();
            initFlipMove();
            initTwistMove();
            initUDSliceMoveConj();
        }
        initMCPermPrun(fullInit);
        initPermCombPPrun(fullInit);
        initSliceTwistPrun(fullInit);
        initSliceFlipPrun(fullInit);
        initTwistFlipPrun(fullInit);
        initLevel = fullInit ? 2 : 1;
    }

    static void setPruning(int[] table, int index, int value) {
        table[index >> 3] ^= value << (index << 2); // index << 2 <=> (index & 7) << 2
    }

    static int getPruning(int[] table, int index) {
        return table[index >> 3] >> (index << 2) & 0xf; // index << 2 <=> (index & 7) << 2
    }

    static void initUDSliceMoveConj() {
        CubieCube c = new CubieCube();
        CubieCube d = new CubieCube();
        for (int i = 0; i < N_SLICE; i++) {
            c.setUDSlice(i);
            for (int j = 0; j < N_MOVES; j += 3) {
                CubieCube.EdgeMult(c, CubieCube.moveCube[j], d);
                UDSliceMove[i][j] = d.getUDSlice();
            }
            for (int j = 0; j < 16; j += 2) {
                CubieCube.EdgeConjugate(c, CubieCube.SymMultInv[0][j], d);
                UDSliceConj[i][j >> 1] = d.getUDSlice();
            }
        }
        for (int i = 0; i < N_SLICE; i++) {
            for (int j = 0; j < N_MOVES; j += 3) {
                int udslice = UDSliceMove[i][j];
                for (int k = 1; k < 3; k++) {
                    udslice = UDSliceMove[udslice][j];
                    UDSliceMove[i][j + k] = udslice;
                }
            }
        }
    }

    static void initFlipMove() {
        CubieCube c = new CubieCube();
        CubieCube d = new CubieCube();
        for (int i = 0; i < N_FLIP_SYM; i++) {
            c.setFlip(CubieCube.FlipS2R[i]);
            for (int j = 0; j < N_MOVES; j++) {
                CubieCube.EdgeMult(c, CubieCube.moveCube[j], d);
                FlipMove[i][j] = d.getFlipSym();
            }
        }
    }

    static void initTwistMove() {
        CubieCube c = new CubieCube();
        CubieCube d = new CubieCube();
        for (int i = 0; i < N_TWIST_SYM; i++) {
            c.setTwist(CubieCube.TwistS2R[i]);
            for (int j = 0; j < N_MOVES; j++) {
                CubieCube.CornMult(c, CubieCube.moveCube[j], d);
                TwistMove[i][j] = d.getTwistSym();
            }
        }
    }

    static void initCPermMove() {
        CubieCube c = new CubieCube();
        CubieCube d = new CubieCube();
        for (int i = 0; i < N_PERM_SYM; i++) {
            c.setCPerm(CubieCube.EPermS2R[i]);
            for (int j = 0; j < N_MOVES2; j++) {
                CubieCube.CornMult(c, CubieCube.moveCube[Util.ud2std[j]], d);
                CPermMove[i][j] = d.getCPermSym();
            }
        }
    }

    static void initEPermMove() {
        CubieCube c = new CubieCube();
        CubieCube d = new CubieCube();
        for (int i = 0; i < N_PERM_SYM; i++) {
            c.setEPerm(CubieCube.EPermS2R[i]);
            for (int j = 0; j < N_MOVES2; j++) {
                CubieCube.EdgeMult(c, CubieCube.moveCube[Util.ud2std[j]], d);
                EPermMove[i][j] = d.getEPermSym();
            }
        }
    }

    static void initMPermMoveConj() {
        CubieCube c = new CubieCube();
        CubieCube d = new CubieCube();
        for (int i = 0; i < N_MPERM; i++) {
            c.setMPerm(i);
            for (int j = 0; j < N_MOVES2; j++) {
                CubieCube.EdgeMult(c, CubieCube.moveCube[Util.ud2std[j]], d);
                MPermMove[i][j] = d.getMPerm();
            }
            for (int j = 0; j < 16; j++) {
                CubieCube.EdgeConjugate(c, CubieCube.SymMultInv[0][j], d);
                MPermConj[i][j] = d.getMPerm();
            }
        }
    }

    static void initCombPMoveConj() {
        CubieCube c = new CubieCube();
        CubieCube d = new CubieCube();
        for (int i = 0; i < N_COMB; i++) {
            c.setCComb(i % 70);
            for (int j = 0; j < N_MOVES2; j++) {
                CubieCube.CornMult(c, CubieCube.moveCube[Util.ud2std[j]], d);
                CCombPMove[i][j] = (d.getCComb() + 70 * ((P2_PARITY_MOVE >> j & 1) ^ (i / 70)));
            }
            for (int j = 0; j < 16; j++) {
                CubieCube.CornConjugate(c, CubieCube.SymMultInv[0][j], d);
                CCombPConj[i][j] = (d.getCComb() + 70 * (i / 70));
            }
        }
    }

    static boolean hasZero(int val) {
        return ((val - 0x11111111) & ~val & 0x88888888) != 0;
    }

    // | 4 bits | 4 bits | 4 bits | 2 bits | 1b | 1b | 4 bits |
    // PrunFlag: | MIN_DEPTH | MAX_DEPTH | INV_DEPTH | Padding | P2 | E2C |
    // SYM_SHIFT |
    static void initRawSymPrun(int[] PrunTable,
            final int[][] RawMove, final int[][] RawConj,
            final int[][] SymMove, final char[] SymState,
            final int PrunFlag, final boolean fullInit) {

        final int SYM_SHIFT = PrunFlag & 0xf;
        final int SYM_E2C_MAGIC = ((PrunFlag >> 4) & 1) == 1 ? CubieCube.SYM_E2C_MAGIC : 0x00000000;
        final boolean IS_PHASE2 = ((PrunFlag >> 5) & 1) == 1;
        final int INV_DEPTH = PrunFlag >> 8 & 0xf;
        final int MAX_DEPTH = PrunFlag >> 12 & 0xf;
        final int MIN_DEPTH = PrunFlag >> 16 & 0xf;
        final int SEARCH_DEPTH = fullInit ? MAX_DEPTH : MIN_DEPTH;

        final int SYM_MASK = (1 << SYM_SHIFT) - 1;
        final boolean ISTFP = RawMove == null;
        final int N_RAW = ISTFP ? N_FLIP : RawMove.length;
        final int N_SIZE = N_RAW * SymMove.length;
        final int N_MOVES = IS_PHASE2 ? 10 : 18;
        final int NEXT_AXIS_MAGIC = N_MOVES == 10 ? 0x42 : 0x92492;

        int depth = getPruning(PrunTable, N_SIZE) - 1;

        if (depth == -1) {
            for (int i = 0; i < N_SIZE / 8 + 1; i++) {
                PrunTable[i] = 0x11111111;
            }
            setPruning(PrunTable, 0, 0 ^ 1);
            depth = 0;
        }

        while (depth < SEARCH_DEPTH) {
            int mask = (depth + 1) * 0x11111111 ^ 0xffffffff;
            for (int i = 0; i < PrunTable.length; i++) {
                int val = PrunTable[i] ^ mask;
                val &= val >> 1;
                PrunTable[i] += val & (val >> 2) & 0x11111111;
            }

            boolean inv = depth > INV_DEPTH;
            int select = inv ? (depth + 2) : depth;
            int selArrMask = select * 0x11111111;
            int check = inv ? depth : (depth + 2);
            depth++;
            int xorVal = depth ^ (depth + 1);
            int val = 0;
            for (int i = 0; i < N_SIZE; i++, val >>= 4) {
                if ((i & 7) == 0) {
                    val = PrunTable[i >> 3];
                    if (!hasZero(val ^ selArrMask)) {
                        i += 7;
                        continue;
                    }
                }
                if ((val & 0xf) != select) {
                    continue;
                }
                int raw = i % N_RAW;
                int sym = i / N_RAW;
                int flip = 0, fsym = 0;
                if (ISTFP) {
                    flip = CubieCube.FlipR2S[raw];
                    fsym = flip & 7;
                    flip >>= 3;
                }

                for (int m = 0; m < N_MOVES; m++) {
                    int symx = SymMove[sym][m];
                    int rawx;
                    if (ISTFP) {
                        rawx = CubieCube.FlipS2RF[FlipMove[flip][CubieCube.Sym8Move[m << 3 | fsym]] ^
                                fsym ^ (symx & SYM_MASK)];
                    } else {
                        rawx = RawConj[RawMove[raw][m]][symx & SYM_MASK];

                    }
                    symx >>= SYM_SHIFT;
                    int idx = symx * N_RAW + rawx;
                    int prun = getPruning(PrunTable, idx);
                    if (prun != check) {
                        if (prun < depth - 1) {
                            m += NEXT_AXIS_MAGIC >> m & 3;
                        }
                        continue;
                    }
                    if (inv) {
                        setPruning(PrunTable, i, xorVal);
                        break;
                    }
                    setPruning(PrunTable, idx, xorVal);
                    for (int j = 1, symState = SymState[symx]; (symState >>= 1) != 0; j++) {
                        if ((symState & 1) != 1) {
                            continue;
                        }
                        int idxx = symx * N_RAW;
                        if (ISTFP) {
                            idxx += CubieCube.FlipS2RF[CubieCube.FlipR2S[rawx] ^ j];
                        } else {
                            idxx += RawConj[rawx][j ^ (SYM_E2C_MAGIC >> (j << 1) & 3)];
                        }
                        if (getPruning(PrunTable, idxx) == check) {
                            setPruning(PrunTable, idxx, xorVal);
                        }
                    }
                }
            }
        }
    }

    static void initTwistFlipPrun(boolean fullInit) {
        initRawSymPrun(
                TwistFlipPrun,
                null, null,
                TwistMove, CubieCube.SymStateTwist, 0x19603,
                fullInit);
    }

    static void initSliceTwistPrun(boolean fullInit) {
        initRawSymPrun(
                UDSliceTwistPrun,
                UDSliceMove, UDSliceConj,
                TwistMove, CubieCube.SymStateTwist, 0x69603,
                fullInit);
    }

    static void initSliceFlipPrun(boolean fullInit) {
        initRawSymPrun(
                UDSliceFlipPrun,
                UDSliceMove, UDSliceConj,
                FlipMove, CubieCube.SymStateFlip, 0x69603,
                fullInit);
    }

    static void initMCPermPrun(boolean fullInit) {
        initRawSymPrun(
                MCPermPrun,
                MPermMove, MPermConj,
                CPermMove, CubieCube.SymStatePerm, 0x8ea34,
                fullInit);
    }

    static void initPermCombPPrun(boolean fullInit) {
        initRawSymPrun(
                EPermCCombPPrun,
                CCombPMove, CCombPConj,
                EPermMove, CubieCube.SymStatePerm, 0x7d824,
                fullInit);
    }
}
