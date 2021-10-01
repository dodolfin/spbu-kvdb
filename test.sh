#!/bin/bash

main () {
  distribution/bin/pf-2021-kvdb -q < $1
}

time main $1
