%  Question 1 -  read in the two images and the given points xy and XYZ
%  and find the camera calibration parameters to explain each image.

close all
readPositions  %  reads given positions of XYZ and xy1 and xy2 for two images

numPositions = size(XYZ,1);

close all

for camera = 1:2
    switch camera
        case 1
            Iname = 'c1.jpg';
            xy = xy1;
        case 2
            Iname = 'c2.jpg';
            xy = xy2;
    end
    
    %  Display image with keypoints
    
    if (1) % (trial == 1)   % only do it first time through
        I = imread(Iname);
        NX = size(I,2);
        NY = size(I,1);
        imageInfo = imfinfo(Iname);
        figure;
        imshow(I);
        title(Iname);
        hold on
        
        %  Draw in green the keypoints locations that were hand selected.
        
        for j = 1:numPositions
            plot(xy(j,1),xy(j,2),'g*');
        end
        
        [P, K, R, C] = calibrate(XYZ, xy);
        
        %  Normalize the K so that the coefficients are more meaningful.
        K = K/K(3,3);
                
        for j = 1:numPositions
            p = P*[ XYZ(j,1) XYZ(j,2) XYZ(j,3)  1]';
            x = p(1)/p(3);
            y = p(2)/p(3);
            
            %  Draw in white square the projected point positions according to the fit model.
            
            plot(ceil(x),ceil(y),'ws');
        end
        
        switch camera
            case 1
                K1 = K;  R1 = R;  C1 = C;
            case 2
                K2 = K;  R2 = R;  C2 = C;
        end
        
    end
end

