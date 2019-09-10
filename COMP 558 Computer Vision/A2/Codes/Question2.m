%read the image, take the blue channel
original_image = imread('james.jpg');
original_image = original_image(:,:,3);
original_image = imresize(original_image,[512 512]);
original_image = imresize(original_image,[256 256]);

%part1
%pre-define the coordinates of pyramid images
coordinate = zeros(4,6);
coordinate = [0.0 0.4 0.6 0.7 0.75 0.78;
              0.2 0.2 0.2 0.2 0.2 0.2;
              0.4 0.2 0.1 0.05 0.025 0.0125;
              0.4 0.2 0.1 0.05 0.025 0.0125;]

%create the Gaussian Pyramid
gausspyramid = cell(6,1);
p = cell(6,1);
for i=1:6
    %initiate pyramid in each level
    if i == 1
        gausspyramid{i} = original_image;
    else
        gausspyramid{i} = gausspyramid{i-1};
    end
    %for each level, smooth it with a Gaussian
    sigma = 2^(i-1);
    filter = fspecial('gaussian',[5 5],sigma);
    gausspyramid{i} = conv2(gausspyramid{i},filter,'same');
    if i ~= 1
        %if not level 0, resize it into half size
        gausspyramid{i} = imresize(gausspyramid{i}, 0.5);
    end
    %create a subplot
%     subplot('Position',coordinate(:,i)); 
%     imshow(uint8(gausspyramid{i}));
    imwrite(uint8(gausspyramid{i}),strcat('Q2_gaussianpyramid_',num2str(i),'.jpg'));
end

%part2
laplacianpyramid = cell(5,1);
for i=1:5
    %upsample pyramid at higher level, and compute differences with the
    %lower one, add a gray intensity of 128 to show both positive and
    %negtive differences
    gausspyramid_low = gausspyramid{i};
    gausspyramid_high = imresize(gausspyramid{i+1},2);
    laplacianpyramid{i} = gausspyramid_high - gausspyramid_low + 128;
%     subplot('Position',coordinate(:,i)); 
%     imshow(uint8(laplacianpyramid{i}));
    imwrite(uint8(laplacianpyramid{i}),strcat('Q2_laplacianpyramid_',num2str(i),'.jpg'));
end

%part3
% s = 1;%sigma
% x = linspace(-10*s,10*s,1000);
% g1 = 1/(2*pi*s)*exp(-x.^2/(2*s^2));%1D gaussina with sigma = 1
% s = 2;
% g2 = 1/(2*pi*s)*exp(-x.^2/(2*s^2));%1D gaussian with sigma = 2
% l = gradient(gradient(g1));
% figure;
% plot(l);%laplacian of Gaussian
% figure;
% plot(g2-g1);%difference of Gaussian

%part4

%find local extremas of DoG
keypoint = [];
for L = 2:4
    dog_current = laplacianpyramid{L};
    dog_previous = imresize(laplacianpyramid{L-1},size(dog_current));
    dog_next = imresize(laplacianpyramid{L+1},[size(dog_current,1) size(dog_current,2)]);
    for i = 2:size(dog_current,1)-1
        for j = 2:size(dog_current,2)-1
            if dog_current(i,j) > dog_current(i,j-1) && dog_current(i,j) > dog_current(i,j+1)
                if dog_current(i,j) > max(dog_current(i-1,j-1:j+1)) && dog_current(i,j) > max(dog_current(i+1,j-1:j+1))
                    if dog_current(i,j) > max(max(dog_previous(i-1:i+1,j-1:j+1))) && dog_current(i,j) > max(max(dog_next(i-1:i+1,j-1:j+1)))
                        temp = [L i j];
                        keypoint = [keypoint;temp];
                    else
                        continue;
                    end
                else
                    continue
                end
            elseif dog_current(i,j) < dog_current(i,j-1) && dog_current(i,j) < dog_current(i,j+1)
                if dog_current(i,j) < min(dog_current(i-1,j-1:j+1)) && dog_current(i,j) < min(dog_current(i+1,j-1:j+1))
                    if dog_current(i,j) < min(min(dog_previous(i-1:i+1,j-1:j+1))) && dog_current(i,j) < min(min(dog_next(i-1:i+1,j-1:j+1)))
                        temp = [L i j];
                        keypoint = [keypoint;temp];
                    else
                        continue;
                    end
                else
                    continue
                end
            else
                continue;
            end
        end
    end
end
color = {'r','g','b'};
a = figure;
imshow(original_image);
hold on;
for n = 1:size(keypoint,1)
    radius = 2^(keypoint(n,1)-1)
    viscircles([keypoint(n,2),keypoint(n,3)].*radius,radius,'LineWidth',0.5,'Color',color{keypoint(n,1)-1});
end
saveas(a,'a.jpg');

