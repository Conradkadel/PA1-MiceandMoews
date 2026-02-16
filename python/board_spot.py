'''
File:    board_spot.py
Author:  Prof Feild
Purpose: Defines the BoardSpot class for representing spots on a game board.
'''

class BoardSpot:
    '''Represents a spot on the graphical game board.'''
    def __init__(self, code, color=None, images=None):
        '''Creates an instance of BoardSpot.
        
        Parameters:
            code (string): A string code representing the type of spot (e.g., 
                          's' for start, 'e' for end, etc.). This is not 
                          interpreted, and so can be whatever makes sense for a 
                          given problem.
            color (tuple): An optional RGB color tuple for the spot. Defaults to 
                           None.
            images (list): An optional list of images associated with the spot. 
                           Multiple images for the same spot will be layered. 
                           Defaults to None.
        '''
        self.code = code # e.g., 's', 'e', 'w', ' '
        self.color = color
        self.images = images if images is not None else []
