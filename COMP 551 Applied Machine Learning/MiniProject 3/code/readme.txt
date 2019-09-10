1.Used library:

numpy
matplotlib
pickle
cv2
keras
sklearn
pillow
scipy
pandas

2.Code info and usage:

'Proposal_corp_pad.py':for pipeline 1, output feature matrices into pickle txt file.

'Raw.py':for pipeline 2, output feature matrices into pickle txt file.

'lenet.py':for running LeNet-5 model, need to change variable 'datasize' to 28 if running pipeline 1.

'alexnet.py':for running AlexNet model, need to change variable 'datasize' to 28 if running pipeline 1.

'resnet.py':for running ResNet-18 model, ensemble and test labels output functions are incorporated. 
To run resnet-101, resnet-152, need to change function called in line 300 to 'build_resnet_101' and 'build_resnet_152' respectively.
To run pipeline 1, need to change 'input_shape' to (1,28,28) in line 279.