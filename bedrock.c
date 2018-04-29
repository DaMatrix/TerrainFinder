/*
Written by ChromeCrusher for finding bases on 2b2t.org

This program requires you to know a top-level bedrock pattern.
A chunk-aligned 16x16 pattern (a complete chunk) is preferable and is the fastest method, but it can find any smaller pattern too (it just takes longer)
You could technically search for an area larger than 16x16 (up to 48x48), but it will be slower and the chances of finding two identical 16x16 bedrock patterns is basically zero.
An 8x8 pattern is usually enough to uniquely identify a location with a fairly low chance of false positives.

You'll need to know which direction is north. This can be found by looking at the textures of some blocks (cobblestone for example, see http://gaming.stackexchange.com/a/23103)
If you can't find which direction is north, you'll have to search for all 4 rotations of the pattern separately. I did not include a function to do it automatically because I haven't needed it.

This program assumes that the chunks being searched were generated in a fairly recent version of Minecraft (probably newer than 1.7)
It will not find 1.3.2 or 1.6.4 chunks, but I don't know exactly when the algorithm changed (it had to do with the structure generation limit changing from y=127 to y=255).

It will work on any Minecraft world that uses normal terrain generation (with the version caveat mentioned above)
It will also work regardless of the world seed, since bedrock patterns are the same in every world.

This program is standalone and doesn't depend on any Java or Minecraft code.

compile with:
   gcc bedrock.c -O3 -o bedrock
run with one of the following for the demos:
  ./bedrock full
  ./bedrock sub
  ./bedrock any

Changelog:
*version 3: fixed problem with non-square search patterns

To change the search pattern, edit one of the arrays below and recompile.
*/

#include <stdio.h>
#include <stdbool.h>
#include <stdlib.h>
#include <stdint.h>
#include <string.h>
#include <time.h>

// the pattern at chunk 123, -456 (real: 1968x, -7296z)
int full_pattern[16*16] = {1,0,0,0,0,0,0,0,1,0,0,0,0,1,0,0,
                           1,1,1,1,0,0,0,0,0,0,0,0,0,1,0,0,
                           1,1,0,0,0,0,0,1,0,0,0,1,0,0,0,0,
                           1,0,1,0,0,0,0,0,0,0,0,0,1,0,0,0,
                           0,1,0,1,0,0,0,0,0,0,1,1,0,0,0,0,
                           0,1,0,0,0,0,1,0,0,0,0,1,0,0,1,0,
                           0,0,1,1,0,1,1,0,0,0,0,1,0,1,0,1,
                           0,0,1,0,1,0,0,0,0,0,0,0,0,1,0,0,
                           0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,
                           0,1,0,0,0,0,0,1,0,1,0,0,0,0,0,1,
                           1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1,
                           0,0,0,0,0,0,0,0,0,1,0,0,1,0,0,0,
                           0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,0,
                           0,0,0,1,0,0,0,0,1,0,0,0,0,0,0,1,
                           0,0,0,0,0,1,0,0,0,0,0,0,1,0,1,0,
                           0,1,1,0,0,0,0,0,0,0,0,0,0,0,1,0};

// a sub pattern of chunk -98, 76 (real: -1568x, 1216x)
int sub_pattern[8*8] = {0,0,1,0,0,0,1,0,
                             0,0,0,0,0,0,0,0,
                             0,0,1,1,0,0,0,0,
                             0,1,1,0,0,0,0,0,
                             0,0,0,1,0,0,0,0,
                             0,0,0,0,0,1,0,1,
                             1,0,0,0,1,0,0,0,
                             0,0,1,0,0,1,0,1};

// a pattern that is part of 4 different chunks (worse case scenario)
// from chunk 34, -56 (real: 544x, -896z)
int any_pattern[8*8] = {0,0,0,0,0,0,0,0,
                        0,0,0,1,0,0,0,0,
                        0,1,0,0,0,0,0,0,
                        0,0,0,1,0,0,0,0,
                        0,1,1,0,0,0,0,0,
                        0,0,0,1,1,0,0,0,
                        0,0,0,0,0,0,0,0,
                        1,0,1,0,0,1,0,0};

// uncomment if you need to use wildcards in your chunk pattern (use 2 instead of 1)
// #define WILDCARD 2

// comment out to hide search times (will not pause upon match if disabled)
#define DEBUG 1

// global variables for returning the position of a subchunk match
int sub_match_x = 0;
int sub_match_z = 0;

// chunk_match checks whether a 16x16 pattern matches the chunk at the given chunk coordinates
// this is the fastest method to locate a chunk, it return 0 at the first mismatch, so it doesn't have to check the entire chunk
// int * c: a row-major 16x16 matrix that represents the top bedrock layer pattern (top left is north-west), 0 = not bedrock, 1 = bedrock, 2 = unknown
// int64_t x: chunk x coordinate (divide x coordinate by 16)
// int64_t z: chunk z coordinate (divide z coordinate by 16)
// returns 1 if it is a match, 0 if it is not
int chunk_match(int * c, int64_t x, int64_t z) {
  // the java.util.Random seed used for bedrock is based on chunk coordinates, not the world seed
  // see world/gen/ChunkProviderOverworld.java: provideChunk from Mod Coder Pack 9.30
  int64_t seed = (x*341873128712LL + z*132897987541LL)^0x5DEECE66DLL;

  // an optimized version of the bedrock pattern generation algorithm
  // see world/biome/Biome.java: generateBiomeTerrain from Mod Coder Pack 9.30
  for(int a = 0; a < 16; ++a) {
    for(int b = 0; b < 16; ++b) {
      // this is equivalent to doing nextInt 250 times
      // it's the main reason this implementation is fast
      // I precalculated the coefficient and constant needed to advance the PRNG to the right position for generating the top-level bedrock pattern
      seed = seed*709490313259657689LL + 1748772144486964054LL;

      // get the value from the PRNG
      seed = seed & ((1LL << 48LL) - 1LL);

      // if defined, this will skip the check
      // use this if you have an almost complete pattern, but are missing some pieces
      // do not #define WILDCARD if you don't need this
#ifdef WILDCARD
      if(c[a*16+b] != WILDCARD)
#endif
      if(4 <= (seed >> 17) % 5) {
        if(c[a*16+b] != 1)
          // if a comparison fails, bail out
          return 0;
      } else {
        if(c[a*16+b] != 0)
          return 0;
      }

      // advances the PRNG a few more times to get ready for the next vertical column
      seed = seed*5985058416696778513LL + -8542997297661424380LL;
    }
  }
  return 1;
}

// sub_chunk_match: submatrix search (slower because it has to compare submatrix across entire chunk)
// use this if you know the pattern is within a chunk, but don't know exactly where in the chunk
// an 8x8 pattern is generally enough to uniquely identify a location, but if you already know the general area you might be able to use a smaller pattern
// int * s: row-major sub-matrix
// char rows: number of rows of *s
// char cols: number of cols of *s
// int64_t x: chunk x
// int64_t z: chunk z
int sub_chunk_match(int * sub, char rows, char cols, int64_t x, int64_t z) {
  int64_t seed = (x*341873128712LL + z*132897987541LL)^0x5DEECE66DLL;

  // generate and store the chunk pattern for searching
  int chunk[256];

  for(int a = 0; a < 16; ++a) {
    for(int b = 0; b < 16; ++b) {
      seed = seed*709490313259657689LL + 1748772144486964054LL;

      seed = seed & ((1LL << 48LL) - 1LL);

      if(4 <= (seed >> 17) % 5) {
        chunk[a*16+b] = 1;
      } else {
        chunk[a*16+b] = 0;
      }

      seed = seed*5985058416696778513LL + -8542997297661424380LL;
    }
  }

  // slow search, tries every possible combination
  bool match;
     for(int m = 0; m <= 16 - rows; m++) {
       for(int n = 0; n <= 16 - cols; n++) {
         match = true;
         for(int i = 0; i < rows && match == true; i++) {
           for(int j = 0; j < cols && match == true; j++) {
             if(sub[i*cols+j] != chunk[(m+i)*16+(n+j)])
               match = false;
           }
         }
         if(match) {
           sub_match_x = m;
           sub_match_z = n;

           return 1;
         }
       }
     }

  return 0;
}

// super_sub_chunk_match works like sub_chunk_match, except that it generates all the surrounding chunks and searches the 48x48 area
// this is the slowest method, only use it you have no idea what the chunk boundaries are
// it could use some further optimization (so it's not constantly regenerating chunks that have already been generated)
// int * s: row-major sub-matrix
// char rows: number of rows of *s
// char cols: number of cols of *s
// int64_t x: chunk x
// int64_t z: chunk z
int super_sub_chunk_match(int * sub, char rows, char cols, int64_t x, int64_t z) {
  int bchunk[48*48];

  for(int64_t i = -1; i <= 1; i++) {
    for(int64_t j = -1; j <= 1; j++) {
      int64_t cx = x + j;
      int64_t cz = z + i;

      int64_t seed = (cx*341873128712LL + cz*132897987541LL)^0x5DEECE66DLL;
      for(int a = 0; a < 16; ++a) {
        for(int b = 0; b < 16; ++b) {
          seed = seed*709490313259657689LL + 1748772144486964054LL;
          seed = seed & ((1LL << 48LL) - 1LL);

          if(4 <= (seed >> 17) % 5) {
            bchunk[(16*(i+1)+a)*48+(16*(j+1)+b)] = 1;
          } else {
            bchunk[(16*(i+1)+a)*48+(16*(j+1)+b)] = 0;
          }

          seed = seed*5985058416696778513LL + -8542997297661424380LL;
        }
      }
    }
  }

  bool match;
  for(int m = 0; m <= 48 - rows; m++) {
    for(int n = 0; n <= 48 - cols; n++) {
      match = true;
      for(int i = 0; i < rows && match == true; i++) {
        for(int j = 0; j < cols && match == true; j++) {
          if(sub[i*cols+j] != bchunk[(m+i)*48+(n+j)])
            match = false;
        }
      }
      if(match) {
        sub_match_x = m;
        sub_match_z = n;

        return 1;
      }
    }
  }

  return 0;
}

// prints a top-level bedrock pattern to stdout
// '1' represents bedrock, '0' represents any other block
// for reference, below is the 0, 0 bedrock pattern (using '*' in place of 1 and ' ' in place of 0 to make it easier to see)
/*
   N
   ^
W<--->E
   V
   S

------------------
|               *|
| *           *  |
|*          *    |
|*         *   * |
|            *   |
|        *  *   *|
|  *  *   ***   *|
|      *   *  *  |
|         *      |
|* *  *  *    *  |
|  *         *   |
|  *        *    |
| * * * *   *    |
|     **    *  * |
|               *|
|               *|
------------------

*/
void print_chunk_pattern(int64_t x, int64_t z) {
  int64_t seed = (x*341873128712LL + z*132897987541LL)^0x5DEECE66DLL;

  for(int a = 0; a < 16; ++a) {
    for(int b = 0; b < 16; ++b) {
      seed = seed*709490313259657689LL + 1748772144486964054LL;

      seed = seed & ((1LL << 48LL) - 1LL);

      if(4 <= (seed >> 17) % 5) {
        printf("1,");
      } else {
        printf("0,");
      }

      seed = seed*5985058416696778513LL + -8542997297661424380LL;
    }
    putchar('\n');
  }
}

// prints the chunk and all the surrounding chunks (48x48 pattern)
void print_super_chunk_pattern(int64_t x, int64_t z) {
  int bchunk[48*48];

  for(int64_t i = -1; i <= 1; i++) {
    for(int64_t j = -1; j <= 1; j++) {
      int64_t cx = x + j;
      int64_t cz = z + i;

      int64_t seed = (cx*341873128712LL + cz*132897987541LL)^0x5DEECE66DLL;
      for(int a = 0; a < 16; ++a) {
        for(int b = 0; b < 16; ++b) {
          seed = seed*709490313259657689LL + 1748772144486964054LL;
          seed = seed & ((1LL << 48LL) - 1LL);

          if(4 <= (seed >> 17) % 5) {
            bchunk[(16*(i+1)+a)*48+(16*(j+1)+b)] = 1;
          } else {
            bchunk[(16*(i+1)+a)*48+(16*(j+1)+b)] = 0;
          }

          seed = seed*5985058416696778513LL + -8542997297661424380LL;
        }
      }
    }
  }
  for(int i = 0; i < 48; i++) {
    for(int j = 0; j < 48; j++) {
      printf("%c,", bchunk[i*48+j] ? '1' : '0');
    }
    printf("\n");
  }
}

// It will search an expanding square pattern starting at the chunks 'start' distance from 0,0 and ending with the chunks 'end' distance from 0,0
int bedrock_finder_fullpattern(int * pattern, int id, int step, int start, int end) {
#ifdef DEBUG
  clock_t cs, ce;
#endif
  for(int r = start + id; r <= end; r += step) {
#ifdef DEBUG
    cs = clock();
#endif
    for(int i = -r; i <= r; i++) {
      if(chunk_match(pattern, i, r)) {
        printf("chunk: (%d, %d), real: (%d, %d)\n", i, r, i*16, r*16);
#ifdef DEBUG
        //print_chunk_pattern(i, r);
        getchar();
#endif
      }
      if(chunk_match(pattern, i, -r)) {
        printf("chunk: (%d, %d), real: (%d, %d)\n", i, -r, i*16, (-r)*16);
#ifdef DEBUG
        //print_chunk_pattern(i, -r);
        getchar();
#endif
      }
    }
    for(int i = -r+1; i < r; i++) {
      if(chunk_match(pattern, r, i)) {
        printf("chunk: (%d, %d), real: (%d, %d)\n", r, i, r*16, i*16);
#ifdef DEBUG
        //print_chunk_pattern(r, i);
        getchar();
#endif
      }
      if(chunk_match(pattern, -r, i)) {
        printf("chunk: (%d, %d), real: (%d, %d)\n", -r, i, (-r)*16, i*16);
#ifdef DEBUG
        //print_chunk_pattern(-r, i);
        getchar();
#endif
      }
    }
#ifdef DEBUG
    ce = clock();
    printf("%d (%f)\n", r, (double)(ce-cs)/CLOCKS_PER_SEC);
#endif
  }
  return 1;
}

int bedrock_finder_subpattern(int * pattern, int rows, int cols, int id, int step, int start, int end) {
#ifdef DEBUG
  clock_t cs, ce;
#endif
  for(int r = start + id; r <= end; r += step) {
#ifdef DEBUG
    cs = clock();
#endif
    for(int i = -r; i <= r; i++) {
      if(sub_chunk_match(pattern, rows, cols, i, r)) {
        printf("chunk: (%d, %d) sub-match at (%d, %d), real: (%d, %d)\n", i, r, sub_match_x, sub_match_z, i*16, r*16);
#ifdef DEBUG
        //print_chunk_pattern(i, r);
        getchar();
#endif
      }
      if(sub_chunk_match(pattern, rows, cols, i, -r)) {
        printf("chunk: (%d, %d) sub-match at (%d, %d), real: (%d, %d)\n", i, -r, sub_match_x, sub_match_z, i*16, (-r)*16);
#ifdef DEBUG
        //print_chunk_pattern(i, -r);
        getchar();
#endif
      }
    }
    for(int i = -r+1; i < r; i++) {
      if(sub_chunk_match(pattern, rows, cols, r, i)) {
        printf("chunk: (%d, %d) sub-match at (%d, %d), real: (%d, %d)\n", r, i, sub_match_x, sub_match_z, r*16, i*16);
#ifdef DEBUG
        //print_chunk_pattern(r, i);
        getchar();
#endif
      }
      if(sub_chunk_match(pattern, rows, cols, -r, i)) {
        printf("chunk: (%d, %d) sub-match at (%d, %d), real: (%d, %d)\n", -r, i, sub_match_x, sub_match_z, (-r)*16, i*16);
#ifdef DEBUG
        //print_chunk_pattern(-r, i);
        getchar();
#endif
      }
    }
#ifdef DEBUG
    ce = clock();
    printf("%d (%f)\n", r, (double)(ce-cs)/CLOCKS_PER_SEC);
#endif
  }
  return 1;
}

int bedrock_finder_anypattern(int * pattern, int rows, int cols, int id, int step, int start, int end) {
#ifdef DEBUG
  clock_t cs, ce;
#endif
  for(int r = start + id; r <= end; r += step) {
#ifdef DEBUG
    cs = clock();
#endif
    for(int i = -r; i <= r; i++) {
      if(super_sub_chunk_match(pattern, rows, cols, i, r)) {
        printf("super chunk: (%d, %d) sub-match at (%d, %d), real: (%d, %d)\n", i, r, sub_match_x, sub_match_z, i*16, r*16);
#ifdef DEBUG
        //print_super_chunk_pattern(i, r);
        getchar();
#endif
      }
      if(super_sub_chunk_match(pattern, rows, cols, i, -r)) {
        printf("super chunk: (%d, %d) sub-match at (%d, %d), real: (%d, %d)\n", i, -r, sub_match_x, sub_match_z, i*16, (-r)*16);
#ifdef DEBUG
        //print_super_chunk_pattern(i, -r);
        getchar();
#endif
      }
    }
    for(int i = -r+1; i < r; i++) {
      if(super_sub_chunk_match(pattern, rows, cols, r, i)) {
        printf("super chunk: (%d, %d) sub-match at (%d, %d), real: (%d, %d)\n", r, i, sub_match_x, sub_match_z, r*16, i*16);
#ifdef DEBUG
        //print_super_chunk_pattern(r, i);
        getchar();
#endif
      }
      if(super_sub_chunk_match(pattern, rows, cols, -r, i)) {
        printf("super chunk: (%d, %d) sub-match at (%d, %d), real: (%d, %d)\n", -r, i, sub_match_x, sub_match_z, (-r)*16, i*16);
#ifdef DEBUG
        //print_super_chunk_pattern(-r, i);
        getchar();
#endif
      }
    }
#ifdef DEBUG
    ce = clock();
    printf("%d (%f)\n", r, (double)(ce-cs)/CLOCKS_PER_SEC);
#endif
  }
  return 1;
}

int main(int argc, char **argv) {

  // to distribute across multiple processors, decide on the number of processes, and launch the program with a process ID
  // for example, to run on 2 processors:
  // first process:  ./bedrock full 0 2
  // second process: ./bedrock full 1 2

  // the optional 4th and 5th arguments define the range to search
  // the 4th argument is the chunk distance from spawn to start the search
  // the 5th argument is the chunk distance from spawn to end the search (defaults to 1875000, which is the edge of the world at 30 million)

  // defaults
  int id = 0;
  int step = 1;
  int start = 0;
  int end = 1875000;

  // help text
  if(argc == 1 || (argc == 2 && (strcmp(argv[1], "-h") == 0 || strcmp(argv[1], "--help") == 0 || strcmp(argv[1], "help") == 0))) {
    fprintf(stderr, "usage: ./bedrock <search type (full|sub|any)> <process id, 0 indexed (default 0)> <number of processes / step value (default 1)> <distance start (default 0)> <distance end (default 1875000)>\n");
    fprintf(stderr, "\nsearch type:\nfull - complete chunk search (fastest method, pattern must be complete chunk)\nsub - sub pattern search within chunk (pattern must be within a single chunk)\nany - arbitrary sub pattern search (slowest method, does not require knowledge of chunk boundaries)\n");
    fprintf(stderr, "\nexample:\n./bedrock full\n");
    return 1;
  }

  if(argc > 2) id = atoi(argv[2]);
  if(argc > 3) step = atoi(argv[3]);
  if(argc > 4) start = atoi(argv[4]);
  if(argc > 5) end = atoi(argv[5]);

  if(strcmp(argv[1], "full") == 0)
    bedrock_finder_fullpattern(full_pattern, id, step, start, end);
  else if(strcmp(argv[1], "sub") == 0)
    bedrock_finder_subpattern(sub_pattern, 8, 8, id, step, start, end);
  else if(strcmp(argv[1], "any") == 0)
    bedrock_finder_anypattern(any_pattern, 8, 8, id, step, start, end);
  else {
    fprintf(stderr, "unknown search type, use 'full', 'sub', or 'any'\n\nexample:\n./bedrock full\n");
    return 1;
  }

  return 0;
}
