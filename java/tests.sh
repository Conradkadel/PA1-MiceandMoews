for board in board1.txt board2.txt board3.txt; do
    for a in ucs astar greedy; do
        echo "java -cp bin csc460.drivers.SearchDriver -p=mm -a=$a -f=../mice-and-meows/$board -h=mice-remaining -graphics=off -search=graph"
        java -cp bin csc460.drivers.SearchDriver -p=mm -a=$a -f=../mice-and-meows/$board -h=mice-remaining -graphics=off -search=graph
    done
done
